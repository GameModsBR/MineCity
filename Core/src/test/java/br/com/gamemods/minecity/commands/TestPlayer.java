package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.world.BlockPos;

public class TestPlayer implements CommandSender
{
    public PlayerID playerId;
    public BlockPos position;

    public TestPlayer(PlayerID playerId, BlockPos position)
    {
        this.playerId = playerId;
        this.position = position;
    }

    @Override
    public BlockPos getPosition()
    {
        return position;
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return playerId;
    }
}
