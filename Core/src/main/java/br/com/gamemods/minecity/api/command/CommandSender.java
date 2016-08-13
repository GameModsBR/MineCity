package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.structure.Selection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandSender
{
    Message CONFIRM_EXPIRED = new Message("cmd.confirm.expired",
            "The action were not confirmed and the confirmation code has expired.");

    EntityPos getPosition();
    boolean isPlayer();
    PlayerID getPlayerId();
    void send(Message message);
    void send(Message[] messages);
    Direction getCardinalDirection();
    Server getServer();

    default boolean isConfirmPending()
    {
        return false;
    }

    default String confirm(@NotNull UFunction<CommandSender, CommandResult<?>> onConfirm)
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    default Selection getSelection(@NotNull WorldDim world)
    {
        throw new UnsupportedOperationException();
    }

    default void giveSelectionTool()
    {
        throw new UnsupportedOperationException();
    }

    default CommandResult<CommandResult<?>> confirm(String code) throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Nullable
    default Message teleport(@NotNull BlockPos pos)
    {
        return teleport(pos.toEntity());
    }

    @Nullable
    default Message teleport(@NotNull EntityPos pos)
    {
        return new Message("action.teleport.unsupported", "Unsupported operation");
    }

    @Nullable
    default MinecraftEntity getMinecraftEntity()
    {
        return null;
    }
}
