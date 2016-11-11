package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandFunction;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EconomyProxy
{
    /**
     * Checks if a player has the amount of money in a given world
     * @param amount must be positive
     * @param world world to be checked
     * @return {@code true} if the player has the amount or if the economy does not support world specific balance
     * and the player has the amount in global scope.
     */
    @Async
    BalanceResult has(@NotNull PlayerID player, double amount, @NotNull WorldDim world);

    /**
     * Checks if a player has an amount of money
     * @param amount must be positive
     */
    @Async
    BalanceResult has(@NotNull PlayerID player, double amount);

    /**
     * Removes an amount of money from the player
     * @param amount must be positive
     * @param simulation if true nothing must be changed, the returned value must be only a simulation
     * @return requested amount - taken amount. Negative values = took too much, positive values = took too few,
     *      if the it returns the same value as "amount" so nothing was taken
     * @throws IllegalArgumentException If amount is negative
     */
    @Async
    OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException;

    /**
     * Gives money to a player, the money should be added to the player's main account if possible. Item based economies with
     * bank support should deposit the value to the player's bank and auto-create it if possible, when it's not possible
     * to credit the bank then the implementation is allowed to give the money to the player in any way.
     * @param amount must be positive
     * @param simulation if true nothing must be changed, the returned value must be only a simulation
     * @return requested amount - taken amount. Negative values = gave too much, positive values = gave too few,
     *      if the it returns the same value as "amount" so nothing was given
     * @throws IllegalArgumentException If amount is negative
     */
    @Async
    OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException;

    /**
     * Removes an amount of money from the player
     * @param amount must be positive
     * @param simulation if true nothing must be changed, the returned value must be only a simulation
     * @return requested amount - taken amount. Negative values = took too much, positive values = took too few,
     *      if the it returns the same value as "amount" so nothing was taken
     * @throws IllegalArgumentException If amount is negative
     */
    @Async
    OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException;

    /**
     * Gives money to a player, the money should be added to the player's main account if possible. Item based economies with
     * bank support should deposit the value to the player's bank and auto-create it if possible, when it's not possible
     * to credit the bank then the implementation is allowed to give the money to the player in any way.
     * @param amount must be positive
     * @param simulation if true nothing must be changed, the returned value must be only a simulation
     * @return requested amount - taken amount. Negative values = gave too much, positive values = gave too few,
     *      if the it returns the same value as "amount" so nothing was given
     * @throws IllegalArgumentException If amount is negative
     */
    @Async
    OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException;

    String format(double amount);

    default boolean isVerbose()
    {
        return true;
    }

    default void refund(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, Throwable reason) throws IllegalArgumentException
    {
        try
        {
            refund(player, amount, balance, world, true);
        }
        catch(Throwable e)
        {
            reason.addSuppressed(e);
        }
    }

    default OperationResult refund(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean verbose) throws IllegalArgumentException
    {
        //TODO Refund message when verbose==true
        return give(player, amount, balance, world, false);
    }

    default OperationResult charge(@NotNull CommandSender player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world) throws IllegalArgumentException
    {
        PlayerID id = player.getPlayerId();
        OperationResult took = take(id, amount, balance, world, false);
        if(!took.success)
            return took;

        double change = took.amount;
        if(change < 0)
        {
            OperationResult give = refund(id, -change, null, world, false);
            change = -give.amount;
        }

        if(change > 0)
        {
            OperationResult give = take(id, change, null, world, false);
            change = give.amount;
        }

        if(isVerbose())
            player.send(CommandFunction.messageSuccess(new Message("economy.paid", "You've paid ${amount}", new Object[]{"amount",format(amount-change)})));
        return new OperationResult(true, change);
    }
}
