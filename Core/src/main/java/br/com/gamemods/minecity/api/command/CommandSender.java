package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandSender
{
    BlockPos getPosition();
    boolean isPlayer();
    PlayerID getPlayerId();
    void send(Message message);

    @Nullable
    Message teleport(@NotNull BlockPos spawn);
}
