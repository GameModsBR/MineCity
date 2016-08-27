package br.com.gamemods.minecity.api;

public class MathUtil
{
    public static final float RADIAN = 0.017453292F;

    public static FloatUnaryOperator sin;

    public static FloatUnaryOperator cos;

    /**
     * Returns the greatest integer less than or equal to the double argument
     */
    public static int floor_double(double value)
    {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }

    public static <C extends Comparable<C>, T extends C> T min(T a, T b)
    {
        return (a.compareTo(b) <= 0)? a : b;
    }

    public static <C extends Comparable<C>, T extends C> T max(T a, T b)
    {
        return (a.compareTo(b) >= 0)? a : b;
    }

    private MathUtil() {}
}
