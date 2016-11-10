package br.com.gamemods.minecity.economy;

public class BalanceResult
{
    public static final BalanceResult TRUE = new BalanceResult(true);
    public static final BalanceResult FALSE = new BalanceResult(false);
    public final boolean result;

    public static BalanceResult of(boolean bool)
    {
        if(bool)
            return TRUE;
        else
            return FALSE;
    }

    public BalanceResult(boolean result)
    {
        this.result = result;
    }
}
