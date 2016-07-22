package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandSender
{
    BlockPos getPosition();
    boolean isPlayer();
    PlayerID getPlayerId();
    void send(Message message);
    Direction getCardinalDirection();

    @Nullable
    Message teleport(@NotNull BlockPos spawn);
}
