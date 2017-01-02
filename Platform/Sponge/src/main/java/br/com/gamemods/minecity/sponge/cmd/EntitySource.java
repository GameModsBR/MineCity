package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.entity.data.supplier.SupplierEntityData;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import br.com.gamemods.minecity.sponge.data.value.SpongeEntityData;
import com.flowpowered.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class EntitySource<E extends Entity, Source extends CommandSource> extends LocatableSource<E, Source>
    implements SupplierEntityData
{
    protected final SpongeEntityData entityData;
    public EntitySource(MineCitySponge server, Source source, E entity)
    {
        super(server, source, entity);
        entityData = (SpongeEntityData) ReactiveLayer.getEntityData(entity).get();
    }

    @NotNull
    @Override
    public SpongeEntityData getEntityData()
    {
        return entityData;
    }

    @Nullable
    @Override
    public MinecraftEntity getMinecraftEntity()
    {
        return server.entity(subject, ()->this);
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

    @Override
    public String toString()
    {
        return "EntitySource{"+
                "entityData="+entityData+
                '}';
    }
}
