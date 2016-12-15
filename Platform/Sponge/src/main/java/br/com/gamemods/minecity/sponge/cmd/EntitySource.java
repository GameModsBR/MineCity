package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import com.flowpowered.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class EntitySource<E extends Entity, Source extends CommandSource> extends LocatableSource<E, Source>
{
    public EntitySource(MineCitySponge server, Source source, E entity)
    {
        super(server, source, entity);
    }

    @Override
    public EntityPos getPosition()
    {
        return server.entityPos(subject);
    }

    @Nullable
    @Override
    public Message teleport(@NotNull EntityPos pos)
    {
        Optional<World> world = server.world(pos.world);
        if(!world.isPresent())
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name",pos.world.name()}
            );

        if(subject.transferToWorld(world.get(), new Vector3d(pos.x, pos.y, pos.z)))
            return null;

        return new Message("action.teleport.cancelled", "The teleport were cancelled");
    }
}
