package de.redsix.dmncheck.util;

import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Make;

import java.util.function.Function;

@Data(value = @Derive(make = {Make.constructors, Make.caseOfMatching, Make.getters}))
public abstract class Either<A, B> {
    public abstract <X> X match(Function<A, X> left, Function<B, X> right);

    public <C> Either<C, B> bind(Function<A, Either<C, B>> function) {
        return this.match(function, Eithers::right);
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
