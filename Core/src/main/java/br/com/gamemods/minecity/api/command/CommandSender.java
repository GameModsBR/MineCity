package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandSender
{
    EntityPos getPosition();
    boolean isPlayer();
    PlayerID getPlayerId();
    void send(Message message);
    void send(Message[] messages);
    Direction getCardinalDirection();
    Server getServer();

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
