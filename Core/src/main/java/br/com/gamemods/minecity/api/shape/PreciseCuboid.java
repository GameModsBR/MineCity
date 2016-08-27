package br.com.gamemods.minecity.api.shape;

public class PreciseCuboid
{
    public final PrecisePoint min, max;

    public PreciseCuboid(PrecisePoint a, PrecisePoint b)
    {
        min = a.compareTo(b) < 0? a : b;
        max = a.compareTo(b) < 0? b : a;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        PreciseCuboid that = (PreciseCuboid) o;

        return min.equals(that.min) && max.equals(that.max);
    }

    @Override
    public int hashCode()
    {
        int result = min.hashCode();
        result = 31*result + max.hashCode();
        return result;
    }
}
