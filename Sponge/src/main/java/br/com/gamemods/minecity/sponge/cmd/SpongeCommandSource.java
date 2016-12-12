package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;

import java.util.Arrays;

public class SpongeCommandSource<Source extends CommandSource> implements CommandSender
{
    public final MineCitySponge server;
    public final Source source;

    public SpongeCommandSource(MineCitySponge server, Source source)
    {
        this.source = source;
        this.server = server;
    }

    @Override
    public void send(Message message)
    {
        source.sendMessage(server.transformer.toText(message));
    }

    @Override
    public void send(Message[] messages)
    {
        source.sendMessages(()-> Arrays.stream(messages).map(server.transformer::toText).iterator());
    }

    @Override
    public EntityPos getPosition()
    {
        return null;
    }

    @Override
    public boolean isPlayer()
    {
        return false;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return null;
    }

    @Override
    public Direction getCardinalDirection()
    {
        return null;
    }

    @NotNull
    @Override
    public Server getServer()
    {
        return server;
    }

    @Override
    public Source getHandler()
    {
        return source;
    }

    @Override
    public boolean isOp()
    {
        return false;
    }
}
