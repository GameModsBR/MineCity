package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.structure.ClaimedChunk;

import java.util.List;

public class CommandEvent
{
    public final MineCity mineCity;
    public final CommandSender sender;
    public final EntityPos position;
    public final List<String> path;
    public final List<String> args;

    public CommandEvent(CommandSender sender, List<String> path, List<String> args)
    {
        this.mineCity = sender.getServer().getMineCity();
        this.sender = sender;
        this.path = path;
        this.args = args;
        position = sender.getPosition();
    }

    public ClaimedChunk getChunk()
    {
        return mineCity.provideChunk(position.getChunk());
    }
}
