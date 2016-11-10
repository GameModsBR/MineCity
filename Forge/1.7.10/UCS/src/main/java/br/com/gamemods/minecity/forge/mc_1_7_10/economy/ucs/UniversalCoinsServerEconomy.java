package br.com.gamemods.minecity.forge.mc_1_7_10.economy.ucs;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.economy.BalanceResult;
import br.com.gamemods.minecity.economy.EconomyProxy;
import br.com.gamemods.minecity.economy.OperationResult;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.ScanResult;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.*;
import net.minecraft.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UniversalCoinsServerEconomy implements EconomyProxy
{
    private PlayerOperator fakePlayer = new PlayerOperator(UUID.nameUUIDFromBytes("MineCity:Fake:UCS:PlayerOperator".getBytes()));

    @NotNull
    private MineCityForge forge;

    public UniversalCoinsServerEconomy(@NotNull MineCityForge forge)
    {
        this.forge = forge;
        try
        {
            UniversalCoinsServer.cardDb.updatePlayerName(fakePlayer.getPlayerId(), "§cMineCityUCFakePlayer");
        }
        catch(Exception e)
        {
            forge.logger.error("Failed to register the MineCity fake player", e);
        }
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount, @NotNull WorldDim world)
    {
        return has(player, amount);
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount)
    {
        try
        {
            return forge.callSyncMethod(()-> {
                EntityPlayerMP forgePlayer = forge.player(player).map(fp-> (EntityPlayerMP) fp.cmd.sender).orElse(null);
                if(forgePlayer == null)
                    return BalanceResult.of(false);

                ScanResult result = UniversalCoinsServerAPI.scanCoins(forgePlayer.inventory);
                return new UCBalance(result.getCoins() >= amount, result);
            }).get(15, TimeUnit.SECONDS);
        }
        catch(InterruptedException | TimeoutException e)
        {
            forge.logger.error("Count not scan "+player+"'s inventory", e);
            return new BalanceResult(false);
        }
        catch(ExecutionException e)
        {
            forge.logger.error("An error has occurred while scanning "+player+"'s inventory", e);
            return new BalanceResult(false);
        }
    }

    @Slow
    @Async
    @Override
    public OperationResult credit(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        try
        {
            PlayerData playerData = UniversalCoinsServer.cardDb.getPlayerData(player.uniqueId);
            AccountAddress address;
            if(playerData.hasPrimaryAccount())
                address = playerData.getPrimaryAccount();
            else
            {
                if(simulation)
                    return new OperationResult(true, 0);

                try
                {
                    address = UniversalCoinsServer.cardDb.createPrimaryAccount(player.uniqueId, player.getName());
                }
                catch(DuplicatedKeyException e)
                {
                    playerData = UniversalCoinsServer.cardDb.getPlayerData(player.uniqueId);
                    if(!playerData.hasPrimaryAccount())
                    {
                        forge.logger.error("Failed to retrieve " + player + "'s primary UCS account", e);
                        return new OperationResult(false, amount, "Failed to find and create " + player.getName() + "'s account");
                    }

                    address = playerData.getPrimaryAccount();
                }
            }

            int coins = (int) amount;
            double fraction = amount - coins;
            if(simulation)
            {
                double delta = amount - -UniversalCoinsServer.cardDb.canDeposit(address, coins);
                return new OperationResult(true, delta + fraction);
            }

            Transaction transaction = new Transaction(null, Transaction.Operation.DEPOSIT_TO_ACCOUNT_BY_API, fakePlayer, null,
                    new Transaction.CardCoinSource(address, coins), null);

            int change = -UniversalCoinsServer.cardDb.depositToAccount(address, coins, transaction);
            return new OperationResult(true, change+fraction);
        }
        catch(DataStoreException | AccountNotFoundException e)
        {
            forge.logger.error("Failed to register a transaction", e);
            return new OperationResult(false, amount, "An error has occurred");
        }
    }

    @Override
    public OperationResult credit(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return credit(player, amount, balance, simulation);
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        try
        {
            double inventory = forge.callSyncMethod(()->{
                ScanResult scan;
                if(balance instanceof UCBalance)
                    scan = ((UCBalance) balance).scan;
                else
                {
                    EntityPlayerMP entity = forge.player(player).map(fp -> (EntityPlayerMP) fp.cmd.sender).orElse(null);
                    if(entity == null)
                        return amount;

                    scan = UniversalCoinsServerAPI.scanCoins(entity.inventory);
                }

                if(simulation)
                    return scan.getCoins() - amount;

                return -UniversalCoinsServerAPI.takeCoins(scan, (int)amount) - amount;
            }).get(15, TimeUnit.SECONDS);

            if(inventory >= 1)
            {
                try
                {
                    PlayerData playerData = UniversalCoinsServer.cardDb.getPlayerData(player.uniqueId);
                    AccountAddress account = playerData.getPrimaryAccount();
                    if(account != null)
                    {
                        int coins = (int) inventory;
                        Transaction transaction = new Transaction(null, Transaction.Operation.WITHDRAW_FROM_ACCOUNT_BY_API, fakePlayer, null, new Transaction.CardCoinSource(account, -coins), null);
                        inventory += UniversalCoinsServer.cardDb.takeFromAccount(account, coins, transaction);
                    }
                }
                catch(DataStoreException | AccountNotFoundException | OutOfCoinsException e)
                {
                    e.printStackTrace();
                }
            }

            return new OperationResult(true, inventory);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return new OperationResult(false, amount, "An error has occurred");
    }

    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return take(player, amount, balance, simulation);
    }

    private class UCBalance extends BalanceResult
    {
        @NotNull
        private final ScanResult scan;

        private UCBalance(boolean result, @NotNull ScanResult scan)
        {
            super(result);
            this.scan = scan;
        }
    }
}
