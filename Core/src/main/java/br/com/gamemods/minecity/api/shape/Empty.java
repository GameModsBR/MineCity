package br.com.gamemods.minecity.api.shape;

import br.com.gamemods.minecity.api.world.ChunkPos;

import java.util.Collections;
import java.util.Iterator;

public class Empty implements Shape
{
    public static final Empty INSTANCE = new Empty();

    private Empty(){}

    @Override
    public int sizeX()
    {
        return 0;
    }

    @Override
    public int sizeY()
    {
        return 0;
    }

    @Override
    public int sizeZ()
    {
        return 0;
    }

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public boolean contains(int x, int y, int z)
    {
        return false;
    }

    @Override
    public boolean affects(ChunkPos chunk)
    {
        return false;
    }

    @Override
    public Iterator<int[]> blockIterator()
    {
        return Collections.emptyIterator();
    }
}
