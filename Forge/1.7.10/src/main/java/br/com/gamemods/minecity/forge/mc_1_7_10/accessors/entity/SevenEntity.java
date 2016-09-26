package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
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

    @Override
    default boolean mount(IEntity entity)
    {
        ((Entity) this).mountEntity((Entity) entity);
        return true;
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

    @Override
    default void setPosAndUpdate(double x, double y, double z)
    {
        Entity entity = (Entity) this;
        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
    }

    @Override
    default boolean sendSpawnPackets(IEntityPlayerMP player)
    {
        Entity entity = (Entity) this;
        Packet pkt = FMLNetworkHandler.getEntitySpawningPacket(entity);
        if(pkt == null)
            return false;

        player.sendPacket(pkt);
        if(!entity.getDataWatcher().getIsBlank())
            player.sendPacket(new S1CPacketEntityMetadata(entity.getEntityId(), entity.getDataWatcher(), true));

        continueSendingSpawnPackets(player);
        return true;
    }

    @Override
    default void sendAllWatchableData(IEntityPlayerMP p)
    {
        Entity entity = (Entity) this;
        p.sendPacket(new S1CPacketEntityMetadata(entity.getEntityId(), entity.getDataWatcher(), true));
    }

    default void continueSendingSpawnPackets(IEntityPlayerMP player)
    {}

    @Override
    default void sendDestroyPacket(IEntityPlayerMP p)
    {
        p.sendPacket(new S13PacketDestroyEntities(((Entity)this).getEntityId()));
    }
}
