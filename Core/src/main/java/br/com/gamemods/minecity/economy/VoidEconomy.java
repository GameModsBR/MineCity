package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public final class VoidEconomy implements EconomyProxy
{
    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount, @NotNull WorldDim world)
    {
        return BalanceResult.TRUE;
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount)
    {
        return BalanceResult.FALSE;
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        return new OperationResult(true, 0);
    }

    @Override
    public OperationResult give(@NotNull PlayerID player, double amount, BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        return new OperationResult(true, amount);
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return take(player, amount, balance, simulation);
    }

    @Override
    public OperationResult give(@NotNull PlayerID player, double amount, BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return give(player, amount, balance, simulation);
    }

    @Override
    public String format(double amount)
    {
        return "$ "+ NumberFormat.getInstance().format(amount);
    }

    @Override
    public boolean isVerbose()
    {
        return false;
    }
}
