package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;

import java.util.Arrays;

public class TestPlayer implements CommandSender
{
    public Server server;
    public PlayerID playerId;
    public EntityPos position;

    public TestPlayer(PlayerID playerId, EntityPos position)
    {
        this.playerId = playerId;
        this.position = position;
    }

    @Override
    public Server getServer()
    {
        return null;
    }

    @Override
    public EntityPos getPosition()
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

    @Override
    public void send(Message message)
    {
        System.out.println(playerId.getName()+": "+message);
    }

    @Override
    public void send(Message[] messages)
    {
        Arrays.stream(messages).forEachOrdered(this::send);
    }

    @Override
    public Direction getCardinalDirection()
    {
        return Direction.NORTH;
    }
}
