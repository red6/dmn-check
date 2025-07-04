package de.redsix.dmncheck.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public sealed interface Either<A, B> {

    record Left<A, B>(A left) implements Either<A, B> {
    }

    record Right<A, B>(B right) implements Either<A, B> {

    }

    default <X> X match(Function<A, X> f, Function<B, X> g) {
        return switch (this) {
            case Left(var left) -> f.apply(left);
            case Right(var right) -> g.apply(right);
        };
    }

    default <C> Either<A, C> bind(Function<B, Either<A, C>> function) {
        return this.match(Either.Left::new, function);
    }

    default <C> Either<A, C> map(Function<B, C> function) {
        return this.match(Either.Left::new, right -> new Either.Right<>(function.apply(right)));
    }

    static <A, B> Collector<Either<A, B>, ?, Either<A, List<B>>> reduce() {
        return Collectors.reducing(new Either.Right<>(new ArrayList<>()),
                either -> either.map(Arrays::asList), (either, eithers) ->
                        either.bind(a -> eithers.bind(listOfA -> new Either.Right<>(appendAll(a, listOfA)))));
    }

    private static <A> List<A> appendAll(List<A> list1, List<A> list2) {
        list1.addAll(list2);
        return list1;
    }


    default Optional<A> getLeft() {
        return this.match(Optional::ofNullable, (__) -> Optional.empty());
    }

    default Optional<B> getRight() {
        return this.match((__) -> Optional.empty(), Optional::ofNullable);
    }
}
