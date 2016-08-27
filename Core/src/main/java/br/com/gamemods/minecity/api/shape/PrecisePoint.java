package br.com.gamemods.minecity.api.shape;

import br.com.gamemods.minecity.api.world.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.function.BiFunction;

public class PrecisePoint implements Serializable, Comparable<PrecisePoint>
{
    private static final long serialVersionUID = -5945672470572274507L;
    public final double x, y, z;

    public PrecisePoint(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PrecisePoint(Point point)
    {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
    }

    @NotNull
    public <T> PrecisePoint apply(@Nullable T x, @Nullable T y, @Nullable T z, @NotNull BiFunction<Double, T, Double> op)
    {
        return new PrecisePoint(op.apply(this.x, x), op.apply(this.y, y), op.apply(this.z, z));
    }

    @NotNull
    public PrecisePoint apply(@NotNull Direction direction, double multiplier, @NotNull BiFunction<Double, Double, Double> op)
    {
        return apply(direction.x*multiplier, direction.y*multiplier, direction.z*multiplier, op);
    }


    @NotNull
    public PrecisePoint add(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> a+b);
    }

    @NotNull
    public PrecisePoint subtract(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> a-b);
    }


    @NotNull
    public PrecisePoint add(@NotNull Direction direction)
    {
        return add(direction.x, direction.y, direction.z);
    }

    @NotNull
    public PrecisePoint subtract(@NotNull Direction direction)
    {
        return subtract(direction.x, direction.y, direction.z);
    }

    @NotNull
    public PrecisePoint add(double x, double y, double z)
    {
        return new PrecisePoint(this.x+x, this.y+y, this.z+z);
    }

    @NotNull
    public PrecisePoint subtract(double x, double y, double z)
    {
        return new PrecisePoint(this.x-x, this.y-y, this.z-z);
    }

    @NotNull
    public PrecisePoint multiply(double x, double y, double z)
    {
        return new PrecisePoint(this.x*x, this.y*y, this.z*z);
    }

    @NotNull
    public PrecisePoint divide(double x, double y, double z)
    {
        return new PrecisePoint(this.x/x, this.y/y, this.z/z);
    }

    @Override
    public int compareTo(@NotNull PrecisePoint o)
    {
        int r = Double.compare(x, o.x);
        if(r != 0)
            return r;
        r = Double.compare(y, o.y);
        if(r != 0)
            return r;
        return Double.compare(z, o.z);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        PrecisePoint that = (PrecisePoint) o;

        return Double.compare(that.x, x) == 0
                && Double.compare(that.y, y) == 0
                && Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp^(temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31*result + (int) (temp^(temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31*result + (int) (temp^(temp >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return "PrecisePoint{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public double distance(Point pos)
    {
        double x = this.x - pos.x;
        double y = this.y - pos.y;
        double z = this.z - pos.z;
        return Math.sqrt(x*x + y*y + z*z);
    }

    public double distance(PrecisePoint pos)
    {
        double x = this.x - pos.x;
        double y = this.y - pos.y;
        double z = this.z - pos.z;
        return Math.sqrt(x*x + y*y + z*z);
    }
}
