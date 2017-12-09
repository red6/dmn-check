package de.redsix.dmncheck.feel.model;

public class RangeExpression implements FeelExpression {

    public final boolean isRightInclusive;
    public final boolean isLeftInclusive;
    public final FeelExpression lowerBound;
    public final FeelExpression upperBound;

    // FIXME Pascal Wittmann, 08.12.2017: How to avoid void here?
    public RangeExpression(boolean isLeftInclusive, FeelExpression lowerBound, Void v, FeelExpression upperBound,  boolean isRightInclusive) {
        this.isLeftInclusive = isLeftInclusive;
        this.isRightInclusive = isRightInclusive;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RangeExpression that = (RangeExpression) o;

        if (isRightInclusive != that.isRightInclusive)
            return false;
        if (isLeftInclusive != that.isLeftInclusive)
            return false;
        if (lowerBound != null ? !lowerBound.equals(that.lowerBound) : that.lowerBound != null)
            return false;
        return upperBound != null ? upperBound.equals(that.upperBound) : that.upperBound == null;
    }

    @Override
    public int hashCode() {
        int result = (isRightInclusive ? 1 : 0);
        result = 31 * result + (isLeftInclusive ? 1 : 0);
        result = 31 * result + (lowerBound != null ? lowerBound.hashCode() : 0);
        result = 31 * result + (upperBound != null ? upperBound.hashCode() : 0);
        return result;
    }
}
