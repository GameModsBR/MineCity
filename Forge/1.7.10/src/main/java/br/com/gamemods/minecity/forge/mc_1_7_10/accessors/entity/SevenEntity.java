package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@Referenced(at = SevenInterfaceTransformer.class)
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
    default boolean isNamed()
    {
        return false;
    }

    @Override
    default void dismount()
    {
        ((Entity) this).mountEntity(null);
    }

    @NotNull
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

    default void writeNBT(NBTTagCompound nbt)
    {
        ((Entity) this).writeToNBT(nbt);
    }

    default void readNBT(NBTTagCompound nbt)
    {
        ((Entity) this).readFromNBT(nbt);
    }
}
