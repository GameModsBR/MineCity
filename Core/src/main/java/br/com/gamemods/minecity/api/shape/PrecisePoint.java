package br.com.gamemods.minecity.api.shape;

public class PrecisePoint
{
    public final double x, y, z;

    public PrecisePoint(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PrecisePoint add(double x, double y, double z)
    {
        return new PrecisePoint(this.x + x, this.y + y, this.z + z);
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
}
