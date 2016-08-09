package br.com.gamemods.minecity.api.shape;

import br.com.gamemods.minecity.api.world.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.function.BiFunction;

public class Point implements Serializable, Comparable<Point>
{
    private static final long serialVersionUID = 806229182488215329L;
    public final int x, y, z;

    public Point(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @NotNull
    public <T> Point apply(@Nullable T x, @Nullable T y, @Nullable T z, @NotNull BiFunction<Integer, T, Integer> op)
    {
        return new Point(op.apply(this.x, x), op.apply(this.y, y), op.apply(this.z, z));
    }

    @NotNull
    public Point apply(@NotNull Direction direction, double multiplier, @NotNull BiFunction<Integer, Double, Integer> op)
    {
        return apply(direction.x*multiplier, direction.y*multiplier, direction.z*multiplier, op);
    }

    @NotNull
    public Point applyI(@NotNull Direction direction, int multiplier, @NotNull BiFunction<Integer, Integer, Integer> op)
    {
        return apply(direction.x*multiplier, direction.y*multiplier, direction.z*multiplier, op);
    }

    @NotNull
    public Point add(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> (int)(a+b));
    }

    @NotNull
    public Point subtract(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> (int)(a-b));
    }


    @NotNull
    public Point add(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a+b);
    }

    @NotNull
    public Point subtract(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a-b);
    }

    @NotNull
    public Point add(@NotNull Direction direction)
    {
        return add(direction.x, direction.y, direction.z);
    }

    @NotNull
    public Point subtract(@NotNull Direction direction)
    {
        return subtract(direction.x, direction.y, direction.z);
    }

    @NotNull
    public Point add(int x, int y, int z)
    {
        return new Point(this.x+x, this.y+y, this.z+z);
    }

    @NotNull
    public Point subtract(int x, int y, int z)
    {
        return new Point(this.x-x, this.y-y, this.z-z);
    }

    @NotNull
    public Point multiply(int x, int y, int z)
    {
        return new Point(this.x*x, this.y*y, this.z*z);
    }

    @NotNull
    public Point divide(int x, int y, int z)
    {
        return new Point(this.x/x, this.y/y, this.z/z);
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

    @Override
    public String toString()
    {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
