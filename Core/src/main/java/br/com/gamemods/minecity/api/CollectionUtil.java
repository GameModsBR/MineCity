package br.com.gamemods.minecity.api;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollectionUtil
{
    /**
     * Returns a list iterator that swaps all previous/next calls.
     * <p><b>Important:</b> The returned iterator violates the {@link ListIterator#nextIndex()} and {@link ListIterator#previousIndex()} specifications.
     */
    public static <E> ListIterator<E> reverse(ListIterator<E> iterator)
    {
        return new ListIterator<E>()
        {
            @Override
            public boolean hasNext()
            {
                return iterator.hasPrevious();
            }

            @Override
            public E next()
            {
                return iterator.previous();
            }

            @Override
            public boolean hasPrevious()
            {
                return iterator.hasNext();
            }

            @Override
            public E previous()
            {
                return iterator.next();
            }

            @Override
            public int nextIndex()
            {
                return iterator.previousIndex();
            }

            @Override
            public int previousIndex()
            {
                return iterator.nextIndex();
            }

            @Override
            public void remove()
            {
                iterator.remove();
            }

            @Override
            public void set(E e)
            {
                iterator.set(e);
            }

            @Override
            public void add(E e)
            {
                iterator.add(e);
            }
        };
    }


    public static <E> Stream<E> reverseStream(ListIterator<E> iterator)
    {
        return stream(reverse(iterator));
    }

    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> sortByValues(Map<K, V> map)
    {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    @SafeVarargs
    public static <R, T extends Supplier<Optional<R>>> Stream<R> optionalStream(T... values)
    {
        return Stream.of(values)
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            ;
    }

    /**
     * @see Collectors#throwingMerger()
     */
    public static <T> BinaryOperator<T> throwingMerger()
    {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

    /**
     * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U, M extends Map<K, U>>
    Collector<T, ?, M> toMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper,
                                    Supplier<M> mapSupplier)
    {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), mapSupplier);
    }

    /**
     * Creates a sequential {@code Stream} using a given {@code Iterator}
     * as the source of elements, with no initial size estimate.
     *
     * @return a sequential {@code Stream} over the remaining items in the iterator
     * @see Spliterators#spliteratorUnknownSize(Iterator, int)
     */
    public static <T> Stream<T> stream(Iterator<T> iterator)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    /**
     * Creates a parallel {@code Stream} using a given {@code Iterator}
     * as the source of elements, with no initial size estimate.
     *
     * @return a parallel {@code Stream} over the remaining items in the iterator
     * @see Spliterators#spliteratorUnknownSize(Iterator, int)
     */
    public static <T> Stream<T> parallelStream(Iterator<T> iterator)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), true);
    }
}
