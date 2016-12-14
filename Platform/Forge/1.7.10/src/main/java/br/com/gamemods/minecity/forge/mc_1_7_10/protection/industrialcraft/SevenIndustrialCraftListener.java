package br.com.gamemods.minecity.forge.mc_1_7_10.protection.industrialcraft;

import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.protection.industrialcraft.IndustrialCraftProtections;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ic2.api.event.ExplosionEvent;
import ic2.api.event.LaserEvent;
import net.minecraft.util.EntityDamageSourceIndirect;

public class SevenIndustrialCraftListener extends IndustrialCraftProtections
{
    public SevenIndustrialCraftListener(MineCityForge mod)
    {
        super(mod);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLaserHitsEntity(LaserEvent.LaserHitsEntityEvent event)
    {
        if(onEntityDamage(
                (IEntity) event.hitentity,
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
                (IWorldServer) event.world,
                event.x, event.y, event.z
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
                new PrecisePoint(event.x, event.y, event.z), event.power,
                (IEntityLivingBase) event.igniter,
                event.rangeLimit, event.radiationRange
        ))
        {
            event.setCanceled(true);
        }
    }
}
