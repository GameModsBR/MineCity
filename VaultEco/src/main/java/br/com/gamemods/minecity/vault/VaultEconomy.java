package br.com.gamemods.minecity.vault;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.economy.BalanceResult;
import br.com.gamemods.minecity.economy.OperationResult;
import br.com.gamemods.minecity.economy.EconomyProxy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VaultEconomy implements EconomyProxy
{
    @NotNull
    private Economy eco;

    public VaultEconomy(@NotNull Economy econ)
    {
        this.eco = econ;
    }

    private OfflinePlayer player(PlayerID player)
    {
        return Bukkit.getOfflinePlayer(player.getUniqueId());
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount, @NotNull WorldDim world)
    {
        return BalanceResult.of(eco.has(player(player), world.dir, amount));
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount)
    {
        return BalanceResult.of(eco.has(player(player), amount));
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        OfflinePlayer bukkitPlayer = player(player);
        if(simulation)
        {
            double delta = eco.getBalance(bukkitPlayer) - amount;
            if(delta < 0)
                return new OperationResult(false, delta, "Insufficient funds");
            return new OperationResult(true, delta);
        }

        EconomyResponse response = eco.withdrawPlayer(bukkitPlayer, amount);
        return new OperationResult(response.type == EconomyResponse.ResponseType.SUCCESS, response.amount, response.errorMessage);
    }

    @Override
    public OperationResult credit(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        OfflinePlayer bukkitPlayer = player(player);
        if(simulation)
        {
            double delta = eco.getBalance(bukkitPlayer) + amount;
            return new OperationResult(true, delta);
        }

        EconomyResponse response = eco.depositPlayer(bukkitPlayer, amount);
        return new OperationResult(response.type == EconomyResponse.ResponseType.SUCCESS, response.amount, response.errorMessage);
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        OfflinePlayer bukkitPlayer = player(player);
        if(simulation)
        {
            double delta = eco.getBalance(bukkitPlayer, world.dir) - amount;
            if(delta < 0)
                return new OperationResult(false, delta, "Insufficient funds");
            return new OperationResult(true, delta);
        }

        EconomyResponse response = eco.withdrawPlayer(bukkitPlayer, world.dir, amount);
        return new OperationResult(response.type == EconomyResponse.ResponseType.SUCCESS, response.amount, response.errorMessage);
    }

    @Override
    public OperationResult credit(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        OfflinePlayer bukkitPlayer = player(player);
        if(simulation)
        {
            double delta = eco.getBalance(bukkitPlayer, world.dir) + amount;
            return new OperationResult(true, delta);
        }

        EconomyResponse response = eco.depositPlayer(bukkitPlayer, world.dir, amount);
        return new OperationResult(response.type == EconomyResponse.ResponseType.SUCCESS, response.amount, response.errorMessage);
    }
}
