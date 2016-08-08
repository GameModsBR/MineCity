package br.com.gamemods.minecity.api.shape;

import org.jetbrains.annotations.NotNull;

public final class Point implements Comparable<Point>
{
    public final int x, y, z;

    public Point(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compareTo(@NotNull Point o)
    {
        int r = x - o.x;
        if(r != 0)
            return r;
        r = z - o.z;
        if(r != 0)
            return r;
        return y - o.y;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        return x == point.x
            && y == point.y
            && z == point.z
            ;
    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31*result + y;
        result = 31*result + z;
        return result;
    }
}
