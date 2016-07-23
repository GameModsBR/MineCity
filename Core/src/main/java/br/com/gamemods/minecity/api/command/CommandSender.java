package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandSender
{
    BlockPos getPosition();
    boolean isPlayer();
    PlayerID getPlayerId();
    void send(Message message);
    void send(Message[] messages);
    Direction getCardinalDirection();

    @Nullable
    default Message teleport(@NotNull BlockPos spawn)
    {
        return new Message("action.teleport.unsupported", "Unsupported operation");
    }

    @Nullable
    default MinecraftEntity getMinecraftEntity()
    {
        return null;
    }
}
