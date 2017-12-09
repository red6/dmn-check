package de.redsix.dmncheck.feel.model;

import java.time.LocalDateTime;

public class DateLiteral implements FeelExpression {
    public final LocalDateTime date;

    public DateLiteral(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DateLiteral that = (DateLiteral) o;

        return date != null ? date.equals(that.date) : that.date == null;
    }

    @Override
    public int hashCode() {
        return date != null ? date.hashCode() : 0;
    }
}
