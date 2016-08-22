package br.com.gamemods.minecity.api.shape;

import br.com.gamemods.minecity.api.world.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Cuboid implements Shape
{
    public final Point min, max;

    public Cuboid(Point a, Point b)
    {
        min = a.compareTo(b) < 0? a : b;
        max = a.compareTo(b) < 0? b : a;
    }

    protected Cuboid(int[] par)
    {
        this(new Point(par[0], par[1], par[2]), new Point(par[3], par[4], par[5]));
    }

    @Override
    public boolean affects(@NotNull ChunkPos chunk)
    {
        // i = mIn
        int ix = chunk.x << 4;
        int iz = chunk.z << 4;
        // a = mAx
        int ax = ix + 15;
        int az = iz + 15;

        return
               // Chunk's min point is inside the shape
               min.x <= ix && max.x >= ix && min.z <= iz && max.z >= iz

               // Chunk's max point is inside the shape
            || min.x <= ax && max.x >= ax && min.z <= az && max.z >= az

               // Shape's min point is inside the chunk
            || ix <= min.x && ax >= min.x && iz <= min.z && az >= min.z

               // Shape's max point is inside the chunk
            || ix <= max.x && ax >= max.x && iz <= max.z && az >= max.z
            ;
    }

    @Override
    public boolean contains(int x, int y, int z)
    {
        return min.x <= x && max.x >= x
                && min.z <= z && max.z >= z
                && min.y <= y && max.y >= y
                ;
    }

    public Iterator<int[]> blockIterator()
    {
        return new Iterator<int[]>()
        {
            private int x = min.x;
            private int z = min.z;
            private int y = min.y;

            @Override
            public boolean hasNext()
            {
                return z < max.z || x < max.x || y < max.y;
            }

            @Override
            public int[] next()
            {
                if(!hasNext())
                    throw new NoSuchElementException();

                int[] ret = {x,y,z};

                if(y < max.y)
                    y++;
                else
                {
                    y = min.y;
                    x++;
                    if(x > max.x)
                    {
                        x = min.x;
                        z ++;
                    }
                }

                return ret;
            }
        };
    }

    @Override
    public boolean overlaps(Shape shape)
    {
        if(shape instanceof Cuboid)
        {
            Cuboid a = this;
            Cuboid b = (Cuboid) shape;

            return a.min.x <= b.max.x
                && a.max.x >= b.min.x
                && a.min.y <= b.max.y
                && a.max.y >= b.min.y
                && a.min.z <= b.max.z
                && a.max.z >= b.min.z
                ;
        }

        return Shape.super.overlaps(shape);
    }

    @Override
    public int sizeX()
    {
        return max.x - min.x + 1;
    }

    public int sizeY()
    {
        return max.y - min.y + 1;
    }

    @Override
    public int sizeZ()
    {
        return max.z - min.z + 1;
    }

    @Override
    public int area()
    {
        return sizeX() * sizeZ() * sizeY();
    }

    public int squareSize()
    {
        return sizeX() * sizeZ();
    }

    @NotNull
    @Override
    public int[] serializationParams()
    {
        return new int[]{min.x, min.y, min.z, max.x, max.y, max.z};
    }

    @NotNull
    @Override
    public Type getType()
    {
        return Type.CUBOID;
    }

    @Override
    public String toString()
    {
        return serializeString();
    }
}
