package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DelayedEconomy extends DelayedProxy<EconomyProxy> implements EconomyProxy
{
    public DelayedEconomy(@NotNull MineCity mineCity, @NotNull EconomyProvider provider)
    {
        super(mineCity, provider);
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount, @NotNull WorldDim world)
    {
        return provide().has(player, amount, world);
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount)
    {
        return provide().has(player, amount);
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        return provide().take(player, amount, balance, simulation);
    }

    @Override
    public OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        return provide().give(player, amount, balance, simulation);
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return provide().take(player, amount, balance, world, simulation);
    }

    @Override
    public OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return provide().give(player, amount, balance, world, simulation);
    }

    @Override
    public String format(double amount)
    {
        return provide().format(amount);
    }

    @Override
    public boolean isVerbose()
    {
        return provide().isVerbose();
    }

    @Override
    public void refund(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, Throwable reason) throws IllegalArgumentException
    {
        provide().refund(player, amount, balance, world, reason);
    }

    @Override
    public OperationResult refund(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean verbose) throws IllegalArgumentException
    {
        return provide().refund(player, amount, balance, world, verbose);
    }

    @Override
    public OperationResult charge(@NotNull CommandSender player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world) throws IllegalArgumentException
    {
        return provide().charge(player, amount, balance, world);
    }
}
