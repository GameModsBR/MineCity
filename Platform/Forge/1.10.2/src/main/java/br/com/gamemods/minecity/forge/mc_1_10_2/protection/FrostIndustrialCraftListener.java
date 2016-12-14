package br.com.gamemods.minecity.forge.mc_1_10_2.protection;

import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.protection.industrialcraft.IndustrialCraftProtections;
import ic2.api.event.ExplosionEvent;
import ic2.api.event.LaserEvent;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FrostIndustrialCraftListener extends IndustrialCraftProtections
{
    public FrostIndustrialCraftListener(MineCityForge mod)
    {
        super(mod);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLaserHitsEntity(LaserEvent.LaserHitsEntityEvent event)
    {
        if(onEntityDamage(
                (IEntity) event.hitEntity,
                (new EntityDamageSourceIndirect("arrow", event.lasershot, event.owner)).setProjectile(),
                event.power,
                false
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLaserHitsBlock(LaserEvent.LaserHitsBlockEvent event)
    {
        if(onLaserHitsBlock(
                (IEntity) event.lasershot,
                (IWorldServer) event.getWorld(),
                event.pos.getX(), event.pos.getY(), event.pos.getZ()
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onExplosionIC2(ExplosionEvent event)
    {
        if(onExplosionIC2(
                (IEntity) event.entity,
                new PrecisePoint(event.pos.xCoord, event.pos.yCoord, event.pos.zCoord), event.power,
                (IEntityLivingBase) event.igniter,
                event.rangeLimit, event.radiationRange
        ))
        {
            event.setCanceled(true);
        }
    }
}
