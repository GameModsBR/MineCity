package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.Sync;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;

import java.util.List;
import java.util.Optional;

public class CommandEvent
{
    public final CommandSender sender;
    public final EntityPos position;
    public final List<String> path;
    public final List<String> args;

    public CommandEvent(CommandSender sender, List<String> path, List<String> args)
    {
        this.sender = sender;
        this.path = path;
        this.args = args;
        position = sender.getPosition();
    }
}
