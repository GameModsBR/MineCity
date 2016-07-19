package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;

public interface CommandSender
{
    BlockPos getPosition();
    boolean isPlayer();
    PlayerID getPlayerId();
    void send(Message message);
}
