package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenEntityTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.List;

@Referenced(at = SevenEntityTransformer.class)
public interface SevenEntity extends IEntity
{
    @Override
    default Entity getVehicle()
    {
        return ((Entity) this).ridingEntity;
    }

    @Override
    default List<Entity> getPassengers()
    {
        Entity passenger = ((Entity) this).riddenByEntity;
        if(passenger == null)
            return Collections.emptyList();
        else
            return Collections.singletonList(passenger);
    }

    @Override
    default String getName()
    {
        return ((Entity) this).getCommandSenderName();
    }

    @Override
    default WorldServer getWorld()
    {
        return (WorldServer) ((Entity) this).worldObj;
    }

    @Override
    default IWorldServer getIWorld()
    {
        return (IWorldServer) ((Entity) this).worldObj;
    }
}
