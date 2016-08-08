package br.com.gamemods.minecity.api;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollectionUtil
{
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
