package br.com.gamemods.minecity.economy;

public final class Tax
{
    private double flat = 0;
    private double percent = 0;

    public Tax(double flat, double percent)
    {
        this.flat = flat;
        this.percent = percent;
    }

    public double getFlat()
    {
        return flat;
    }

    public double getPercent()
    {
        return percent;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Tax tax = (Tax) o;

        return Double.compare(tax.flat, flat) == 0 && Double.compare(tax.percent, percent) == 0;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(flat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(percent);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return "Tax{" +
                "flat=" + flat +
                ", percent=" + percent +
                '}';
    }
}
