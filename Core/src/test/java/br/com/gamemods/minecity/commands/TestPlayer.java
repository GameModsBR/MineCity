package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TestPlayer implements CommandSender
{
    public Server server;
    public PlayerID playerId;
    public EntityPos position;

    public TestPlayer(Server server, PlayerID playerId, EntityPos position)
    {
        this.server = server;
        this.playerId = playerId;
        this.position = position;
    }

    @Override
    public boolean hasPermission(String perm)
    {
        return true;
    }

    @NotNull
    @Override
    public Server getServer()
    {
        return server;
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
