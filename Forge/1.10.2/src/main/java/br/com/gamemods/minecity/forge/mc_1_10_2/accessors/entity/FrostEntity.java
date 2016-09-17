package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostEntity extends IEntity, ICommander
{
    @NotNull
    @Override
    default String getName()
    {
        return IEntity.super.getName();
    }

    @Override
    default boolean sendSpawnPackets(IEntityPlayerMP player)
    {
        Entity entity = (Entity) this;
        Packet pkt = FMLNetworkHandler.getEntitySpawningPacket(entity);
        if(pkt == null)
            return false;

        player.sendPacket(pkt);
        if(!entity.getDataManager().isEmpty())
            player.sendPacket(new SPacketEntityMetadata(entity.getEntityId(), entity.getDataManager(), true));

        continueSendingSpawnPackets(player);
        return true;
    }

    default void continueSendingSpawnPackets(IEntityPlayerMP player)
    {}
}
