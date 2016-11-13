package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.ServerAdmins;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.structure.Selection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;

/**
 * Represents something that can execute commands
 */
public interface CommandSender extends Permissible
{
    Message CONFIRM_EXPIRED = new Message("cmd.confirm.expired",
            "The action were not confirmed and the confirmation code has expired.");

    /**
     * Gets the sender position when the sender is locatable
     * @return The position or {@code null} when no position is available
     */
    EntityPos getPosition();

    /**
     * Checks if this sender is a player
     * @return {@code true} if this is a player
     */
    boolean isPlayer();

    /**
     * Gets a PlayerID if {@link #isPlayer()} is {@code true}
     * @return The player id or {@code null} if this is not a player
     */
    PlayerID getPlayerId();

    /**
     * Gets the direction that this sender is looking
     * @return The direction or {@code null} if this sender is not in the world
     */
    Direction getCardinalDirection();

    /**
     * Gets the server instance
     */
    @NotNull
    Server getServer();

    Object getHandler();

    default boolean hasPermission(String perm)
    {
        return getServer().getMineCity().permission.hasPermission(this, perm);
    }

    /**
     * Checks if this sender has any confirmation pending
     */
    default boolean isConfirmPending()
    {
        return false;
    }

    /**
     * Prepare a dangerous function to be executed only after confirmation.
     * @param onConfirm The function to be executed.
     * @return A new confirmation code
     */
    default String confirm(@NotNull UFunction<CommandSender, CommandResult<?>> onConfirm)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Verify a previous confirmation code, if it matches then execute the prepared function.
     * The code must not be usable again and the function must be invoked only once.
     * @param code The confirmation code typed by the player
     * @return An unsuccessful result without message if the code is invalid, or the result returned bt the prepared
     * function if it's valid.
     * @throws ExecutionException If the prepared function throws an exception
     */
    default CommandResult<CommandResult<?>> confirm(String code) throws ExecutionException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the current block selection. If there's no selection in that world then a new selection is created
     * and the previous selection is discarded.
     * @throws UnsupportedOperationException if this is not a player
     */
    @NotNull
    default Selection getSelection(@NotNull WorldDim world)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException if this is not a player
     */
    default void giveSelectionTool()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Teleports this sender to a given position
     * @param pos The position
     * @return {@code null} if the teleport was successful or an message informing why the teleportation failed
     */
    @Nullable
    default Message teleport(@NotNull BlockPos pos)
    {
        return teleport(pos.toEntity());
    }

    /**
     * Teleports this sender to a given position
     * @param pos The position
     * @return {@code null} if the teleport was successful or an message informing why the teleportation failed
     */
    @Nullable
    default Message teleport(@NotNull EntityPos pos)
    {
        return new Message("action.teleport.unsupported", "Unsupported operation");
    }

    /**
     * Gets the entity represented by this command sender.
     * @return {@code null} if this is not an entity
     */
    @Nullable
    default MinecraftEntity getMinecraftEntity()
    {
        return null;
    }

    boolean isOp();

    /**
     * If this is a player, returns the player id, if this is an entity, returns the entity id otherwise returns the server admins id
     */
    @NotNull
    @Override
    default Identity<?> identity()
    {
        PlayerID playerId = getPlayerId();
        if(playerId != null)
            return playerId;

        MinecraftEntity entity = getMinecraftEntity();
        if(entity != null)
            return entity.getIdentity();

        return ServerAdmins.INSTANCE;
    }

    void toggleAutoClaim();

    boolean getAutoClaim();
}
