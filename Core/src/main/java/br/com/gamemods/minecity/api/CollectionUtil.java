package br.com.gamemods.minecity.api;

import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
}
