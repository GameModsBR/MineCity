package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.world.Locatable;

public class LocatableSource<L extends Locatable, Source extends CommandSource> extends SpongeCommandSource<L, Source>
{
    public LocatableSource(MineCitySponge server, Source source, L locatable)
    {
        super(server, locatable, source);
    }

    @Override
    public EntityPos getPosition()
    {
        return server.entityPos(subject.getLocation());
    }
}
