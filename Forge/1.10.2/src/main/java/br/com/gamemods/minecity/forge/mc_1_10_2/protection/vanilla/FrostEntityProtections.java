package br.com.gamemods.minecity.forge.mc_1_10_2.protection.vanilla;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FrostEntityProtections extends EntityProtections
{
    public FrostEntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event)
    {
        if(event.getWorld().isRemote)
            return;

        if(onPlayerInteractEntity(
                (IEntityPlayerMP) event.getEntityPlayer(),
                (IEntity) event.getTarget(),
                (IItemStack) (Object) event.getItemStack(),
                event.getHand() == EnumHand.OFF_HAND
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityConstruct(EntityEvent.EntityConstructing event)
    {
        if(event.getEntity().worldObj.isRemote)
            return;

        mod.callSpawnListeners((IEntity) event.getEntity());
    }

    //@SubscribeEvent(priority = EventPriority.HIGH)
    public void on(EntityEvent.EnteringChunk event)
    {
        if(event.getEntity().worldObj.isRemote)
            return;

        onEntityEnterChunk(
                (IEntity) event.getEntity(),
                event.getOldChunkX(),
                event.getOldChunkZ(),
                event.getNewChunkX(),
                event.getNewChunkZ()
        );
    }
}
