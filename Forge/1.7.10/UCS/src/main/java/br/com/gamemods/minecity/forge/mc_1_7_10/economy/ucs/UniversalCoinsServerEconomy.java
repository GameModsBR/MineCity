package br.com.gamemods.minecity.forge.mc_1_7_10.economy.ucs;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.Sync;
import br.com.gamemods.minecity.api.command.CommandFunction;
import br.com.gamemods.minecity.api.command.Message;
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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
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

    @Sync
    private Set<AccountAddress> findAccounts(EntityPlayerMP player)
    {
        InventoryPlayer inventory = player.inventory;
        int size = inventory.getSizeInventory();
        Set<AccountAddress> addressList = new HashSet<>(2);
        for(int i = 0; i < size; i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if(UniversalCoinsServerAPI.canCardBeUsedBy(stack, player))
                addressList.add(UniversalCoinsServerAPI.getAddress(stack));
        }
        return addressList;
    }

    @Override
    public BalanceResult has(@NotNull PlayerID player, double amount)
    {
        try
        {
            BalanceResult result = forge.callSyncMethod(()-> {
                EntityPlayerMP forgePlayer = forge.player(player).map(fp-> (EntityPlayerMP) fp.cmd.sender).orElse(null);
                if(forgePlayer == null)
                    return BalanceResult.of(false);

                ScanResult scan = UniversalCoinsServerAPI.scanCoins(forgePlayer.inventory);
                return new UCBalance(scan.getCoins() >= amount, scan, findAccounts(forgePlayer));
            }).get(15, TimeUnit.SECONDS);

            if(result.result || !(result instanceof UCBalance))
                return result;

            for(AccountAddress account : ((UCBalance) result).accounts)
            {
                try
                {
                    if(UniversalCoinsServer.cardDb.getAccountBalance(account) >= amount)
                        return new UCBalance(true, ((UCBalance) result).scan, ((UCBalance) result).accounts);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            return result;
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
    public OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
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

            int change = UniversalCoinsServer.cardDb.depositToAccount(address, coins, transaction);
            AccountAddress finalAddress = address;
            forge.callSyncMethod(()->
                forge.player(player).ifPresent(fp -> fp.send(CommandFunction.messageSuccess(new Message(
                        "economy.deposited", "${amount} have been deposited to your account ${name}",
                        new Object[][]{
                                {"amount", format(amount - (change + fraction))},
                                {"name", finalAddress.getName()+"/"+finalAddress.getNumber()}
                        }
                ))))
            );
            return new OperationResult(true, change+fraction);
        }
        catch(DataStoreException | AccountNotFoundException e)
        {
            forge.logger.error("Failed to register a transaction", e);
            return new OperationResult(false, amount, "An error has occurred");
        }
    }

    @Override
    public OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        return give(player, amount, balance, simulation);
    }

    @Override
    public OperationResult refund(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean verbose) throws IllegalArgumentException
    {
        try
        {
            double inventory = forge.callSyncMethod(()->{
                ScanResult scan;
                EntityPlayerMP entity = forge.player(player).map(fp -> (EntityPlayerMP) fp.cmd.sender).orElse(null);
                if(entity == null)
                    return amount;

                scan = UniversalCoinsServerAPI.scanCoins(entity.inventory);
                return (double) UniversalCoinsServerAPI.giveCoins(scan, entity, (int)amount, 0);
            }).get(15, TimeUnit.SECONDS);

            OperationResult result;
            if(inventory >= 1)
                result = give(player, inventory, null, world, false);
            else
                result = new OperationResult(true, inventory);

            if(verbose)
                forge.callSyncMethod(()->
                    forge.player(player).ifPresent(fp -> fp.send(CommandFunction.messageSuccess(new Message(
                            "economy.refunded", "You've been refunded ${amount}",
                            new Object[]{"amount", format(amount - result.amount)}
                    ))))
                );

            return result;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return new OperationResult(false, amount, "An error has occurred");
    }

    private double deduce(PlayerID player, AccountAddress account, double amount) throws AccountNotFoundException, DataStoreException, OutOfCoinsException
    {
        int coins = (int)Math.ceil(amount);
        Transaction transaction = new Transaction(null, Transaction.Operation.WITHDRAW_FROM_ACCOUNT_BY_API, fakePlayer, null, new Transaction.CardCoinSource(account, -coins), null);
        UniversalCoinsServer.cardDb.takeFromAccount(account, coins, transaction);
        double change = amount-coins;
        forge.callSyncMethod(()->
            forge.player(player).ifPresent(fp -> fp.send(CommandFunction.messageSuccess(new Message(
                    "economy.deduced", "${amount} have been deduced from your account ${name}",
                    new Object[][]{
                            {"amount", format(amount - change)},
                            {"name", account.getName()+"/"+account.getNumber()}
                    }
            ))))
        );
        return change;
    }

    @Slow
    @Async
    @Override
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        if(amount < 0)
            throw new IllegalArgumentException("Negative amount: "+amount);

        try
        {
            if(balance instanceof UCBalance && !((UCBalance) balance).accounts.isEmpty())
            {
                for(AccountAddress account : ((UCBalance) balance).accounts)
                {
                    try
                    {
                        if(UniversalCoinsServer.cardDb.getAccountBalance(account) >= amount)
                        {
                            amount = deduce(player, account, amount);
                            if(amount <= 0)
                                return new OperationResult(true, amount);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            double remaining = amount;
            double inventory = forge.callSyncMethod(()->{
                ScanResult scan;
                if(balance instanceof UCBalance)
                    scan = ((UCBalance) balance).scan;
                else
                {
                    EntityPlayerMP entity = forge.player(player).map(fp -> (EntityPlayerMP) fp.cmd.sender).orElse(null);
                    if(entity == null)
                        return remaining;

                    scan = UniversalCoinsServerAPI.scanCoins(entity.inventory);
                }

                if(simulation)
                    return scan.getCoins() - remaining;

                return (double) UniversalCoinsServerAPI.takeCoins(scan, (int)Math.ceil(remaining));
            }).get(15, TimeUnit.SECONDS);

            if(inventory >= 1)
            {
                try
                {
                    PlayerData playerData = UniversalCoinsServer.cardDb.getPlayerData(player.uniqueId);
                    AccountAddress account = playerData.getPrimaryAccount();
                    if(account != null)
                        inventory += deduce(player, account, inventory);
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

    @Override
    public String format(double amount)
    {
        long coins = (long) amount;
        String name = StatCollector.translateToLocal("item.itemCoin.name");
        return NumberFormat.getInstance().format(coins)+" "+name+(coins == 1?"":"s");
    }

    private class UCBalance extends BalanceResult
    {
        @NotNull
        private final ScanResult scan;
        private final Set<AccountAddress> accounts;

        private UCBalance(boolean result, @NotNull ScanResult scan, Set<AccountAddress> accounts)
        {
            super(result);
            this.scan = scan;
            this.accounts = accounts;
        }
    }
}
