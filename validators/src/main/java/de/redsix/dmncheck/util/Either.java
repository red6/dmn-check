package de.redsix.dmncheck.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Make;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Data(value = @Derive(make = {Make.constructors, Make.caseOfMatching, Make.getters}))
public abstract class Either<A, B> {
    public abstract <X> X match(Function<A, X> left, Function<B, X> right);

    public <C> Either<A, C> bind(Function<B, Either<A, C>> function) {
        return this.match(Eithers::left, function);
    }

    public <C> Either<A, C> map(Function<B, C> function) {
        return this.match(Eithers::left, right -> Eithers.right(function.apply(right)));
    }

    public static <A, B> Collector<Either<A, B>, ?, Either<A, List<B>>> reduce() {
        return Collectors.reducing(
                Eithers.right(new ArrayList<>()),
                either -> either.map(Arrays::asList),
                (either, eithers) -> either.bind(a -> eithers.bind(listOfA -> Eithers.right(appendAll(a, listOfA)))));
    }

    private static <A> List<A> appendAll(List<A> list1, List<A> list2) {
        list1.addAll(list2);
        return list1;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public abstract String toString();
}
