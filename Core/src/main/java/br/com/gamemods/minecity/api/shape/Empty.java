package br.com.gamemods.minecity.api.shape;

import br.com.gamemods.minecity.api.world.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public class Empty implements Shape
{
    public static final Empty INSTANCE = new Empty();

    private Empty(){}

    @NotNull
    @Override
    public Type getType()
    {
        return Type.EMPTY;
    }

    @NotNull
    @Override
    public int[] serializationParams()
    {
        return new int[0];
    }

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
    public int area()
    {
        return 0;
    }

    @Override
    public int squareSize()
    {
        return 0;
    }

    @Override
    public boolean contains(int x, int y, int z)
    {
        return false;
    }

    @Override
    public boolean affects(@NotNull ChunkPos chunk)
    {
        return false;
    }

    @Override
    public boolean overlaps(Shape shape)
    {
        return false;
    }

    @Override
    public Iterator<int[]> blockIterator()
    {
        return Collections.emptyIterator();
    }

    @Override
    public String toString()
    {
        return "Shape:EMPTY";
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj.getClass() == getClass();
    }

    @Override
    public int hashCode()
    {
        return 0;
    }
}
