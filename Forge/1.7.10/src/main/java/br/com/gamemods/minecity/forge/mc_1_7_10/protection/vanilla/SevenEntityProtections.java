package br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPotionEffect;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityXPOrb;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.EntityProjectile;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityArrow;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityFishHook;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IExplosion;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockState;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.*;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.MineCitySevenHooks;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.ExplosionEvent;

import java.util.AbstractList;
import java.util.List;

public class SevenEntityProtections extends EntityProtections
{
    public SevenEntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onExplosion(ExplosionEvent.Detonate event)
    {
        if(event.world.isRemote)
            return;

        onExplosionDetonate(
                (IEntity) MineCitySevenHooks.spawner,
                (IWorldServer) event.world,
                (IExplosion) event.explosion,
                (List) event.getAffectedEntities(),
                new AbstractList<BlockPos>()
                {
                    List<ChunkPosition> base = event.getAffectedBlocks();
                    IWorldServer world = (IWorldServer) event.world;
                    BlockPos last;

                    @Override
                    public BlockPos get(int index)
                    {
                        ChunkPosition cp = base.get(index);
                        BlockPos bp;
                        if(last == null)
                            last = bp = new BlockPos(mod.world(world), cp.chunkPosX, cp.chunkPosY, cp.chunkPosZ);
                        else
                            last = bp = new BlockPos(last, cp.chunkPosX, cp.chunkPosY, cp.chunkPosZ);

                        bp.getChunk();
                        return bp;
                    }

                    @Override
                    public BlockPos set(int index, BlockPos pos)
                    {
                        BlockPos prev = get(index);
                        base.set(index, new ChunkPosition(pos.x, pos.y, pos.z));
                        return prev;
                    }

                    @Override
                    public BlockPos remove(int index)
                    {
                        BlockPos removed = get(index);
                        base.remove(index);
                        return removed;
                    }

                    @Override
                    public boolean remove(Object o)
                    {
                        if(o instanceof BlockPos)
                        {
                            BlockPos pos = (BlockPos) o;
                            return base.remove(new ChunkPosition(pos.x, pos.y, pos.z));
                        }
                        return false;
                    }

                    @Override
                    public void add(int index, BlockPos pos)
                    {
                        base.add(index, new ChunkPosition(pos.x, pos.y, pos.z));
                    }

                    @Override
                    public boolean add(BlockPos pos)
                    {
                        return base.add(new ChunkPosition(pos.x, pos.y, pos.z));
                    }

                    @Override
                    public int size()
                    {
                        return base.size();
                    }
                }
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreImpact(PreImpactEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPreImpact(
                (IEntity) event.entity,
                (IRayTraceResult) event.traceResult
        ))
        {
            event.setCanceled(true);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPostImpact(PostImpactEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPostImpact(
                (IEntity) event.entity,
                (IRayTraceResult) event.traceResult,
                (List) event.changes
        ))
        {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityEnterWorld(EntityJoinWorldEvent event)
    {
        if(event.world.isRemote)
            return;

        Entity entity = event.entity;
        if(onEntityEnterWorld(
                (IEntity) entity,
                new br.com.gamemods.minecity.api.world.BlockPos(mod.world(event.world), (int) entity.posX, (int) entity.posY, (int) entity.posZ),
                (IEntity) MineCitySevenHooks.spawner
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEggSpawnChicken(EggSpawnChickenEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onEggSpawnChicken((EntityProjectile) event.entity))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntitySpawnByFishingHook(EntitySpawnByFishingHookEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        onEntitySpawnByFishingHook(
                (IEntity) event.entity,
                (IEntityFishHook) event.hook
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDropsXp(LivingExpDropEvent event)
    {
        if(event.entityLiving.worldObj.isRemote)
            return;

        onLivingDropsExp(
                (IEntityLivingBase) event.entityLiving,
                (IEntityPlayerMP) event.attackingPlayer,
                event.droppedExperiencePoints
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingDrops(LivingDropsEvent event)
    {
        if(event.entityLiving.worldObj.isRemote)
            return;

        onLivingDrops(
                (IEntityLivingBase) event.entityLiving,
                event.source,
                event.drops
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerDrops(PlayerDropsEvent event)
    {
        if(event.entityPlayer.worldObj.isRemote)
            return;

        onPlayerDrops(
                (IEntityPlayerMP) event.entityPlayer,
                event.source,
                event.drops
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onItemToss(ItemTossEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onItemToss(
                (IEntityPlayerMP) event.player,
                (IEntityItem) event.entityItem
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onXpOrbTargetPlayerEvent(XpOrbTargetPlayerEvent event)
    {
        if(event.entityPlayer.worldObj.isRemote)
            return;

        if(onXpOrbTargetPlayerEvent(
                (IEntityPlayerMP) event.entityPlayer,
                (IEntityXPOrb) event.orb
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerPickupExpEvent(PlayerPickupXpEvent event)
    {
        if(event.entityPlayer.worldObj.isRemote)
            return;

        if(onPlayerPickupExpEvent(
                (IEntityPlayerMP) event.entityPlayer,
                (IEntityXPOrb) event.orb
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerPickupArrowEvent(PlayerPickupArrowEvent event)
    {
        if(event.arrow.worldObj.isRemote)
            return;

        if(onPlayerPickupArrowEvent(
                (IEntityPlayerMP) event.entityPlayer,
                (IEntityArrow) event.arrow
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onProjectileModifyBlock(ProjectileModifyBlockEvent event)
    {
        if(event.world.isRemote)
            return;

        if(onProjectileModifyBlock(
                (IEntity) event.projectile,
                new SevenBlockState(event.block, event.blockMetadata),
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
        if(event.entity.worldObj != null)
        {
            if(event.entity.worldObj.isRemote)
                return;
        }
        else
        {
            if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                return;
        }

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
    public void onPlayerPickupItem(EntityItemPickupEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPlayerPickupItem(
                (IEntityPlayerMP) event.entityPlayer,
                (IEntityItem) event.item,
                false
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
    public void onPlayerAttack(AttackEntityEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPlayerAttack(
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
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntityLivingBase) event.entity,
                event.source,
                event.ammount,
                false
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
                event.amount,
                false
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
                event.amount,
                false
        ))
        {
            event.setCanceled(true);
        }
    }
}
