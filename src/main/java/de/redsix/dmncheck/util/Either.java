package de.redsix.dmncheck.util;

import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Make;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

import static de.redsix.dmncheck.util.Eithers.left;

@Data(value = @Derive(make = {Make.constructors, Make.caseOfMatching, Make.getters}))
public abstract class Either<A, B> {
    public abstract <X> X match(Function<A, X> left, Function<B, X> right);

    public <C> Either<C, B> bind(Function<A, Either<C, B>> function) {
        return this.match(function, Eithers::right);
    }

    public <C> Either<C, B> map(Function<A, C> function) {
        return this.match(left -> left(function.apply(left)), Eithers::right);
    }

    public static <A, B> Collector<Either<A, B>, Either<List<A>, B>, Either<List<A>, B>> sequence() {
        return Collector.of(() -> left(new ArrayList<>()),
                (eithers, either) -> either.bind(a -> eithers.bind(listOfA -> left(append(a, listOfA)))),
                (either1, either2) -> either2.bind(a2 -> either1.bind(a1 -> left(appendAll(a1, a2)))));
    }


    private static <A> List<A> append(A element, List<A> list) {
        list.add(element);
        return list;
    }

    private static <A> List<A> appendAll(List<A> list1, List<A> list2) {
        list1.addAll(list2);
        return list1;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
