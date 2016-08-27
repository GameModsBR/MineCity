package br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

public class SevenEntityProtections extends EntityProtections
{
    public SevenEntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(EntityInteractEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPlayerInteractEntity(
                (IEntityPlayerMP) event.entityPlayer,
                (IEntity) event.target,
                (IItemStack) (Object) event.entityPlayer.getHeldItem(),
                false
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityConstruct(EntityEvent.EntityConstructing event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        mod.callSpawnListeners((IEntity) event.entity);
    }

    //@SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityEnterChunk(EntityEvent.EnteringChunk event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        onEntityEnterChunk(
                (IEntity) event.entity,
                event.oldChunkX,
                event.oldChunkZ,
                event.newChunkX,
                event.newChunkZ
        );
    }
}
