package br.com.gamemods.minecity.forge.mc_1_7_10.protection;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.OnImpact;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockDragonEggTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockPistonBaseTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockTNTTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.GrowMonitorTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.*;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.AdapterTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.InventoryTransferDClassTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.UpgradeTractorBeamTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockState;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityLivingBaseTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityPotionTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.*;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import scala.Option;

import java.util.ArrayList;
import java.util.List;

@Referenced
public class MineCitySevenHooks
{
    public static Entity spawner;
    public static Object pistonMovedBy;

    @Referenced(at = UpgradeTractorBeamTransformer.class)
    public static void setPistonMovedBy(Object cause)
    {
        pistonMovedBy = cause;
    }

    @Referenced(at = BlockPistonBaseTransformer.class)
    public static boolean onPistonMove(boolean ret, Throwable ex, Object blockObj, World world, int x, int y, int z, int dir)
            throws Throwable
    {
        world.captureBlockSnapshots = false;
        List<BlockSnapshot> changes = new ArrayList<>(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();
        Object movedBy = pistonMovedBy;
        pistonMovedBy = null;

        try
        {
            SevenBlockState state = changes.stream()
                    .filter(snap -> snap.x == x && snap.y == y && snap.z == z)
                    .map(IBlockSnapshot.class::cast).map(IBlockSnapshot::getReplacedState).map(SevenBlockState.class::cast)
                    .findFirst().orElseGet(() -> (SevenBlockState) ((IWorldServer)world).getIState(x,y,z));

            if(MinecraftForge.EVENT_BUS.post(new PistonMoveEvent(world, x, y, z, state, dir, false, changes, movedBy)))
            {
                revertChanges(changes);
                ret = false;
            }
            else
                sendUpdates(changes);
        }
        catch(Exception e)
        {
            revertChanges(changes);
            throw e;
        }

        if(ex != null)
            throw ex;

        return ret;
    }

    @Referenced(at = PathFinderTransformer.class)
    public static boolean onPathFind(PathFinder pathFinder, PathPoint point, IBlockAccess access, EntityLiving entity)
    {
        return ModEnv.entityProtections.onPathFind(pathFinder, point, access, entity);
    }

    @Referenced(at = OnImpactTransformer.class)
    public static void onImpact(Entity entity, MovingObjectPosition result)
    {
        if(MinecraftForge.EVENT_BUS.post(new PreImpactEvent(entity, result)))
        {
            entity.setDead();
            return;
        }

        World worldObj = entity.worldObj;
        try
        {
            spawner = entity;
            worldObj.captureBlockSnapshots = true;

            ((OnImpact) entity).mineCityOnImpact(result);

            worldObj.captureBlockSnapshots = false;
            spawner = null;

            ArrayList<BlockSnapshot> changes = new ArrayList<>(worldObj.capturedBlockSnapshots);
            worldObj.capturedBlockSnapshots.clear();

            if(MinecraftForge.EVENT_BUS.post(new PostImpactEvent(entity, result, changes)))
                revertChanges(changes);
            else
                sendUpdates(changes);
        }
        catch(Exception e)
        {
            revertChanges(new ArrayList<>(worldObj.capturedBlockSnapshots));
            throw e;
        }
        finally
        {
            spawner = null;
            worldObj.captureBlockSnapshots = false;
            worldObj.capturedBlockSnapshots.clear();
        }
    }

    @Referenced(at = OnImpactTransformer.class)
    public static void onFireBallImpact(EntityFireball fireball, MovingObjectPosition result)
    {
        onImpact(fireball, result);
    }

    @Referenced(at = OnImpactTransformer.class)
    public static void onThrowableImpact(EntityThrowable throwable, MovingObjectPosition result)
    {
        onImpact(throwable, result);
    }

    @SuppressWarnings("unchecked")
    private static void revertChanges(List<BlockSnapshot> changes)
    {
        MineCityForge.snapshotHandler.restore((List) changes);
    }

    @SuppressWarnings("unchecked")
    private static void sendUpdates(List<BlockSnapshot> changes)
    {
        MineCityForge.snapshotHandler.send((List) changes);
    }

    @Contract("!null, _, _, _, _, _ -> fail")
    @Referenced(at = GrowMonitorTransformer.class)
    public static void onGrowableGrow(Throwable thrown, Object source, World world, int x, int y, int z)
            throws Throwable
    {
        world.captureBlockSnapshots = false;
        ArrayList<BlockSnapshot> changes = new ArrayList<>(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        if(MinecraftForge.EVENT_BUS.post(new BlockGrowEvent(world, x, y, z, source, changes)))
            revertChanges(changes);
        else
            sendUpdates(changes);

        if(thrown != null)
            throw thrown;
    }

    @Referenced(at = BlockDragonEggTransformer.class)
    public static void startCapturingBlocks(World world)
    {
        world.captureBlockSnapshots = true;
    }

    @Referenced(at = BlockDragonEggTransformer.class)
    public static void onDragonEggTeleport(BlockDragonEgg block, EntityPlayer player, World world, int x, int y, int z, int meta)
    {
        world.captureBlockSnapshots = false;
        ArrayList<BlockSnapshot> changes = new ArrayList<>(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        if(MinecraftForge.EVENT_BUS.post(new PlayerTeleportDragonEggEvent(player, x, y, z, world, block, meta, changes)))
            changes.forEach(snapshot -> {
                world.restoringBlockSnapshots = true;
                snapshot.restore(true, false);
                world.restoringBlockSnapshots = false;
            });
    }

    @Referenced(at = EntityFishingHookTransformer.class)
    public static Entity onFishingHookSpawnEntity(Entity entity, EntityFishHook hook)
    {
        MinecraftForge.EVENT_BUS.post(new EntitySpawnByFishingHookEvent(entity, hook));
        return entity;
    }

    @Referenced(at = SevenEntityLivingBaseTransformer.class)
    public static int getExperienceDrop(int exp, EntityLivingBase living, EntityPlayer player)
    {
        LivingExpDropEvent event = new LivingExpDropEvent(living, player, exp);
        if (MinecraftForge.EVENT_BUS.post(event))
            return 0;

        return event.droppedExperiencePoints;
    }

    @Contract("null, _ -> null")
    @Referenced(at = EntityXPOrbTransformer.class)
    public static EntityPlayer onXpOrbTargetPlayer(EntityPlayer player, EntityXPOrb orb)
    {
        if(player == null)
            return null;

        Event event = new XpOrbTargetPlayerEvent(player, orb);
        if(MinecraftForge.EVENT_BUS.post(event))
            return null;
        else
            return player;
    }

    @Referenced(at = EntityArrowTransformer.class)
    public static boolean onPlayerPickupArrow(EntityArrow arrow, EntityPlayer player)
    {
        Event event = new PlayerPickupArrowEvent(player, arrow);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = EntityIgnitionTransformer.class)
    public static void onIgnite(Entity entity, int fireTicks, @Nullable Object source, Class<?> sourceClass, String method, String desc)
    {
        Event event;
        if(source instanceof Entity)
            event = new EntityIgniteEntityEvent(entity, (Entity) source, fireTicks, source, sourceClass, method, desc);
        else
            event = new EntityIgniteEvent(entity, fireTicks, source, sourceClass, method, desc);

        if(!MinecraftForge.EVENT_BUS.post(event))
            entity.setFire(fireTicks);
    }

    @Referenced(at = EntityEnderCrystalTransformer.class)
    public static boolean onEntityDamage(Entity entity, DamageSource source, float amount)
    {
        EntityDamageEvent event = new EntityDamageEvent(entity, source, amount);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = BlockTNTTransformer.class)
    public static boolean onArrowIgnite(World world, int x, int y, int z, Block block, EntityArrow arrow)
    {
        ProjectileModifyBlockEvent event = new ProjectileModifyBlockEvent(arrow, world, x, y, z, block);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = EntityBoatTransformer.class)
    @Referenced(at = EntityMinecartTransformer.class)
    public static boolean onVehicleDamage(Entity entity, DamageSource source, float amount)
    {
        return MinecraftForge.EVENT_BUS.post(new VehicleDamageEvent(entity, source, amount));
    }

    @Referenced(at = SevenEntityPotionTransformer.class)
    public static void onPotionApplyEffect(EntityLivingBase entity, PotionEffect effect, Entity potion)
    {
        PotionApplyEvent event = new PotionApplyEvent(entity, effect, potion);
        if(!MinecraftForge.EVENT_BUS.post(event))
            entity.addPotionEffect(effect);
    }

    @Referenced(at = EntityFishingHookTransformer.class)
    public static Entity onFishingHookHitEntity(Entity entity, EntityFishHook hook)
    {
        if(entity == null)
            return null;

        FishingHookHitEntityEvent event = new FishingHookHitEntityEvent(entity, hook);
        if(MinecraftForge.EVENT_BUS.post(event))
            return null;
        else
            return entity;
    }

    @Referenced(at = EntityFishingHookTransformer.class)
    public static boolean onFishingHookBringEntity(EntityFishHook hook)
    {
        FishingHookBringEntityEvent event = new FishingHookBringEntityEvent(hook.field_146043_c, hook);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = AdapterTransformer.class)
    public static Point toPoint(int x, int y, int z)
    {
        return new Point(x, y, z);
    }

    @Referenced(at = InventoryTransferDClassTransformer.class)
    @SuppressWarnings("unchecked")
    public static BlockPos toPos(Object obj, int x, int y, int z)
    {
        Option<IWorldServer> opt = (Option<IWorldServer>) obj;
        return new BlockPos(ModEnv.blockProtections.mod.world(opt.get()), x, y, z);
    }

    private MineCitySevenHooks(){}
}
