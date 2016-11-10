package br.com.gamemods.minecity.economy;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
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
    OperationResult credit(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException;

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
    OperationResult credit(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException;
}
