package br.com.gamemods.minecity.api;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CollectionUtil
{
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> sortByValues(Map<K, V> map)
    {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
                (Comparator<Map.Entry<K, V>>) (e1, e2) -> {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
