package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

public final class VoidEconomy implements EconomyProxy
{
    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount, @NotNull WorldDim world)
    {
        return BalanceResult.FALSE;
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount)
    {
        return BalanceResult.FALSE;
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        return new OperationResult(false, amount, "Not Supported");
    }

    @Override
    public OperationResult credit(@NotNull PlayerID player, double amount, BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        return new OperationResult(false, amount, "Not Supported");
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return take(player, amount, balance, simulation);
    }

    @Override
    public OperationResult credit(@NotNull PlayerID player, double amount, BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return credit(player, amount, balance, simulation);
    }
}
