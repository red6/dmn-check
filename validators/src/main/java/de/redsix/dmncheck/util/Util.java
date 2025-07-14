package de.redsix.dmncheck.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Util {

    private Util() {}

    public static <A, B, C> Stream<C> zip(
        final Stream<? extends A> a,
        final Stream<? extends B> b,
        final BiFunction<? super A, ? super B, ? extends C> zipper
    ) {
        Objects.requireNonNull(zipper);
        final Spliterator<? extends A> aSpliterator = Objects.requireNonNull(
            a
        ).spliterator();
        final Spliterator<? extends B> bSpliterator = Objects.requireNonNull(
            b
        ).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        final int characteristics =
            aSpliterator.characteristics() &
            bSpliterator.characteristics() &
            ~(Spliterator.DISTINCT | Spliterator.SORTED);

        final long zipSize = ((characteristics & Spliterator.SIZED) != 0)
            ? Math.min(
                aSpliterator.getExactSizeIfKnown(),
                bSpliterator.getExactSizeIfKnown()
            )
            : -1;

        final Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
        final Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
        final Iterator<C> cIterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public C next() {
                return zipper.apply(aIterator.next(), bIterator.next());
            }
        };

        final Spliterator<C> split = Spliterators.spliterator(
            cIterator,
            zipSize,
            characteristics
        );
        return (a.isParallel() || b.isParallel())
            ? StreamSupport.stream(split, true)
            : StreamSupport.stream(split, false);
    }

    public static <A, B, C, D> Stream<D> zip(
        final Stream<? extends A> a,
        final Stream<? extends B> b,
        final Stream<? extends C> c,
        final TriFunction<? super A, ? super B, ? super C, ? extends D> zipper
    ) {
        Objects.requireNonNull(zipper);
        final Spliterator<? extends A> aSpliterator = Objects.requireNonNull(
            a
        ).spliterator();
        final Spliterator<? extends B> bSpliterator = Objects.requireNonNull(
            b
        ).spliterator();
        final Spliterator<? extends C> cSpliterator = Objects.requireNonNull(
            c
        ).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        final int characteristics =
            aSpliterator.characteristics() &
            bSpliterator.characteristics() &
            cSpliterator.characteristics() &
            ~(Spliterator.DISTINCT | Spliterator.SORTED);

        final long zipSize = ((characteristics & Spliterator.SIZED) != 0)
            ? Math.min(
                aSpliterator.getExactSizeIfKnown(),
                Math.min(
                    bSpliterator.getExactSizeIfKnown(),
                    cSpliterator.getExactSizeIfKnown()
                )
            )
            : -1;

        final Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
        final Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
        final Iterator<C> cIterator = Spliterators.iterator(cSpliterator);

        final Iterator<D> dIterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return (
                    aIterator.hasNext() &&
                    bIterator.hasNext() &&
                    cIterator.hasNext()
                );
            }

            @Override
            public D next() {
                return zipper.apply(
                    aIterator.next(),
                    bIterator.next(),
                    cIterator.next()
                );
            }
        };

        final Spliterator<D> split = Spliterators.spliterator(
            dIterator,
            zipSize,
            characteristics
        );
        return (a.isParallel() || b.isParallel() || c.isParallel())
            ? StreamSupport.stream(split, true)
            : StreamSupport.stream(split, false);
    }
}
