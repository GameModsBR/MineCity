package br.com.gamemods.minecity.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheMap<K,V> extends LinkedHashMap<K,V>
{
    private static final long serialVersionUID = 5960937865915279203L;
    private int size;

    public CacheMap(int capacity)
    {
        super(capacity);
        this.size = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        return size() >= size;
    }
}
