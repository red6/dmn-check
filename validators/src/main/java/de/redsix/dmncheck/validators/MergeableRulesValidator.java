package de.redsix.dmncheck.validators;

import de.redsix.dmncheck.feel.FeelExpression;
import de.redsix.dmncheck.feel.FeelParser;
import de.redsix.dmncheck.feel.Operator;
import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.util.Expression;
import de.redsix.dmncheck.util.TopLevelExpressionLanguage;
import de.redsix.dmncheck.validators.core.SimpleValidator;
import de.redsix.dmncheck.validators.core.ValidationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * Proposes to merge rules with identical outputs that differ in only one input entry. The merged rule uses the
 * disjunction of both entries, e.g., {@code "Fall", "Winter"}.
 */
public class MergeableRulesValidator extends SimpleValidator<DecisionTable> {

    // For other hit policies the order or the number of matching rules changes the result
    private static final Set<HitPolicy> HIT_POLICIES = Set.of(HitPolicy.UNIQUE, HitPolicy.ANY);

    private TopLevelExpressionLanguage toplevelExpressionLanguage = new TopLevelExpressionLanguage(null);

    @Override
    public Optional<String> documentation() {
        return Optional.of("https://github.com/red6/dmn-check#mergeable-rules");
    }

    @Override
    public List<ValidationResult> apply(final DmnModelInstance dmnModelInstance) {
        toplevelExpressionLanguage =
                new TopLevelExpressionLanguage(dmnModelInstance.getDefinitions().getExpressionLanguage());
        return super.apply(dmnModelInstance);
    }

    @Override
    public Class<DecisionTable> getClassUnderValidation() {
        return DecisionTable.class;
    }

    @Override
    public boolean isApplicable(final DecisionTable decisionTable, final ValidationContext validationContext) {
        return HIT_POLICIES.contains(decisionTable.getHitPolicy());
    }

    @Override
    public List<ValidationResult> validate(final DecisionTable decisionTable, final ValidationContext validationContext) {
        final List<Input> inputs = new ArrayList<>(decisionTable.getInputs());
        final List<Rule> rules = new ArrayList<>(decisionTable.getRules());
        final List<ValidationResult> results = new ArrayList<>();

        for (int j = 0; j < rules.size(); j++) {
            for (int i = 0; i < j; i++) {
                final Optional<ValidationResult> result = checkMergeable(inputs, rules.get(i), rules.get(j));
                if (result.isPresent()) {
                    results.add(result.get());
                    break;
                }
            }
        }
        return results;
    }

    private Optional<ValidationResult> checkMergeable(final List<Input> inputs, final Rule rule, final Rule otherRule) {
        final List<InputEntry> inputEntries = new ArrayList<>(rule.getInputEntries());
        final List<InputEntry> otherInputEntries = new ArrayList<>(otherRule.getInputEntries());

        if (inputEntries.size() != inputs.size()
                || otherInputEntries.size() != inputs.size()
                || !texts(rule.getOutputEntries()).equals(texts(otherRule.getOutputEntries()))) {
            return Optional.empty();
        }

        final List<Integer> differingColumns = IntStream.range(0, inputs.size())
                .filter(column -> !text(inputEntries.get(column)).equals(text(otherInputEntries.get(column))))
                .boxed()
                .toList();

        // Rules without differences are reported by the DuplicateRuleValidator
        if (differingColumns.size() != 1) {
            return Optional.empty();
        }

        final int column = differingColumns.getFirst();
        final InputEntry entry = inputEntries.get(column);
        final InputEntry otherEntry = otherInputEntries.get(column);
        if (!isDisjunctionOperand(entry) || !isDisjunctionOperand(otherEntry)) {
            return Optional.empty();
        }

        return Optional.of(ValidationResult.init
                .message("Rule can be merged with rule " + rule.getId() + " as both only differ in input "
                        + columnName(inputs.get(column), column) + ". Use '" + text(entry) + ", " + text(otherEntry)
                        + "' as input entry of the merged rule")
                .severity(Severity.WARNING)
                .element(otherRule)
                .build());
    }

    /** Empty entries and negations cannot be combined with other entries in a FEEL disjunction. */
    private boolean isDisjunctionOperand(final InputEntry inputEntry) {
        final Expression expression = toplevelExpressionLanguage.toExpression(inputEntry);
        return FeelParser.parse(expression)
                .match(
                        __ -> false,
                        feelExpression -> switch (feelExpression) {
                            case FeelExpression.Empty() -> false;
                            case FeelExpression.NaryExpression(final var operator, final var __)
                                    when operator == Operator.NOT -> false;
                            default -> true;
                        });
    }

    private static String columnName(final Input input, final int column) {
        return input.getLabel() != null ? "'" + input.getLabel() + "'" : String.valueOf(column + 1);
    }

    private static List<String> texts(final Iterable<? extends ModelElementInstance> elements) {
        final List<String> texts = new ArrayList<>();
        elements.forEach(element -> texts.add(text(element)));
        return texts;
    }

    private static String text(final ModelElementInstance element) {
        return element.getTextContent().strip();
    }
}
