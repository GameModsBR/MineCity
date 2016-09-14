package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.world.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class BlameOtherInheritedReaction extends BlameOtherReaction
{
    @NotNull
    private Reaction inherit;

    public BlameOtherInheritedReaction(@NotNull MineCity mineCity, @NotNull BlockPos pos, @NotNull Reaction inherit)
    {
        this(mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).owner(), inherit);
    }

    public BlameOtherInheritedReaction(Permissible other,
                                       @NotNull Reaction inherit)
    {
        super(other);
        this.inherit = inherit;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return inherit.stream(mineCity, other);
    }
}
