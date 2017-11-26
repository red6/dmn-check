package de.redsix.dmncheck.validators;

public class  ValidationResult {

    private final ValidationResultType validationResultType;

    private final String message;

    private ValidationResult(String message) {
        this.message = message;
        this.validationResultType = ValidationResultType.ERROR;
    }

    static ValidationResult from(String message) {
        return new ValidationResult(message);
    }

    @Override
    public String toString() {
        return message;
    }
}
