package de.redsix.dmncheck.feel;

import org.derive4j.Data;

import java.util.Arrays;

import static de.redsix.dmncheck.feel.ExpressionTypes.DOUBLE;
import static de.redsix.dmncheck.feel.ExpressionTypes.INTEGER;
import static de.redsix.dmncheck.feel.ExpressionTypes.LONG;
import static de.redsix.dmncheck.feel.ExpressionTypes.TOP;

@Data
public abstract class ExpressionType {

    public interface Cases<R> {
        R TOP();
        R STRING();
        R BOOLEAN();
        R INTEGER();
        R LONG();
        R DOUBLE();
        R DATE();
        R ENUM(String className);
    }

    public abstract <R> R match(ExpressionType.Cases<R> cases);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    public static boolean isNumeric(final ExpressionType givenType) {
        return !TOP().equals(givenType) && Arrays.asList(INTEGER(), LONG(), DOUBLE()).contains(givenType);
    }

    public boolean isSubtypeOf(final ExpressionType supertype) {
        return reflexivity(this, supertype) || TOPisTopElement(supertype) || INTEGERsubtypeOfLONG(this, supertype)
                || INTEGERsubtypeOfDOUBLE(this, supertype);
    }

    private boolean reflexivity(final ExpressionType subtype, final ExpressionType supertype) {
        return subtype.equals(supertype);
    }

    private boolean TOPisTopElement(final ExpressionType supertype) {
        return TOP().equals(supertype);
    }

    private boolean INTEGERsubtypeOfLONG(final ExpressionType subtype, final ExpressionType supertype) {
        return INTEGER().equals(subtype) && LONG().equals(supertype);
    }

    private boolean INTEGERsubtypeOfDOUBLE(final ExpressionType subtype, final ExpressionType supertype) {
        return INTEGER().equals(subtype) && DOUBLE().equals(supertype);
    }
}
