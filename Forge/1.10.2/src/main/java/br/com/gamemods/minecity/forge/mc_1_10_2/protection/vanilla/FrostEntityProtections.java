package br.com.gamemods.minecity.forge.mc_1_10_2.protection.vanilla;

import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.*;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
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
    public void onProjectileModifyBlock(ProjectileModifyBlockEvent event)
    {
        if(event.getWorld().isRemote)
            return;

        BlockPos pos = event.getPos();
        if(onProjectileModifyBlock(
                (IEntity) event.projectile,
                (IState) event.getState(),
                (IWorldServer) event.getWorld(),
                pos.getX(), pos.getY(), pos.getZ()
        ))
        {
            event.setCanceled(true);
        }
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
    public void onPlayerInteractEntityPrecisely(PlayerInteractEntityPreciseEvent event)
    {
        if(event.player.worldObj.isRemote)
            return;

        Vec3d pos = event.pos;
        if(onPlayerInteractEntityPrecisely(
                (IEntityPlayerMP) event.player,
                (IEntity) event.getEntity(),
                (IItemStack) (Object) event.stack,
                event.hand == EnumHand.OFF_HAND,
                new PrecisePoint(pos.xCoord, pos.yCoord, pos.zCoord)
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityEnterChunk(EntityEvent.EnteringChunk event)
    {
        if(event.getEntity().worldObj.isRemote)
            return;

        onEntityEnterChunk(
                event.getEntity(),
                event.getOldChunkX(),
                event.getOldChunkZ(),
                event.getNewChunkX(),
                event.getNewChunkZ()
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFishingHookHitEntity(FishingHookHitEntityEvent event)
    {
        if(event.hook.worldObj.isRemote)
            return;

        if(onFishingHookHitEntity(
                (IEntity) event.getEntity(),
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
                (IEntity) event.getEntity(),
                (EntityProjectile) event.hook
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPotionApply(PotionApplyEvent event)
    {
        if(event.getEntity().worldObj.isRemote)
            return;

        if(onPotionApply(
                (IEntityLivingBase) event.getEntityLiving(),
                (IPotionEffect) event.effect,
                (IEntity) event.potion
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(EntityItemPickupEvent event)
    {
        if(event.getEntity().worldObj.isRemote)
            return;

        if(onPlayerPickupItem(
                (IEntityPlayerMP) event.getEntityPlayer(),
                (IEntityItem) event.getItem()
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
                (IEntity) event.getEntity(),
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
        if(event.getEntity().worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntityLivingBase) event.getEntityLiving(),
                event.getSource(),
                event.getAmount()
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onVehicleDamage(VehicleDamageEvent event)
    {
        if(event.getEntity().worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntity) event.getEntity(),
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
        if(event.getEntity().worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntity) event.getEntity(),
                event.source,
                event.amount
        ))
        {
            event.setCanceled(true);
        }
    }
}
