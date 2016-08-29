package br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.*;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockState;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.*;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

public class SevenEntityProtections extends EntityProtections
{
    public SevenEntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onProjectileModifyBlock(ProjectileModifyBlockEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onProjectileModifyBlock(
                (IEntity) event.projectile,
                event.blockMetadata == 0? (IState) event.block : new SevenBlockState(event.block, event.blockMetadata),
                (IWorldServer) event.world,
                event.x, event.y, event.z
        ))
        {
            event.setCanceled(true);
        }
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityEnterChunk(EntityEvent.EnteringChunk event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        onEntityEnterChunk(
                event.entity,
                event.oldChunkX,
                event.oldChunkZ,
                event.newChunkX,
                event.newChunkZ
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFishingHookHitEntity(FishingHookHitEntityEvent event)
    {
        if(event.hook.worldObj.isRemote)
            return;

        if(onFishingHookHitEntity(
                (IEntity) event.entity,
                (EntityProjectile) event.hook
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFishingHookBringEntity(FishingHookBringEntityEvent event)
    {
        if(event.hook.worldObj.isRemote)
            return;

        if(onFishingHookBringEntity(
                (IEntity) event.entity,
                (EntityProjectile) event.hook
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPotionApply(PotionApplyEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPotionApply(
                (IEntityLivingBase) event.entity,
                (IPotionEffect) event.effect,
                (IEntity) event.potion
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityIgniteEntityEvent(EntityIgniteEntityEvent event)
    {
        if(event.igniter.worldObj.isRemote)
            return;

        if(onEntityIgniteEntityEvent(
                (IEntity) event.entity,
                (IEntity) event.igniter,
                event.ticks
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntityLivingBase) event.entity,
                event.source,
                event.ammount
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onVehicleDamage(VehicleDamageEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntity) event.entity,
                event.source,
                event.amount
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntity) event.entity,
                event.source,
                event.amount
        ))
        {
            event.setCanceled(true);
        }
    }
}
