package br.com.gamemods.minecity.forge.mc_1_7_10.protection;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenBlockDragonEggTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenBlockTNTTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenGrowMonitorTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.*;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.*;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

@Referenced
public class MineCitySevenHooks
{
    @Contract("!null, _, _, _, _, _ -> fail")
    @Referenced(at = SevenGrowMonitorTransformer.class)
    public static void onGrowableGrow(Throwable thrown, Object source, World world, int x, int y, int z)
            throws Throwable
    {
        world.captureBlockSnapshots = false;
        ArrayList<BlockSnapshot> changes = new ArrayList<>(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        if(MinecraftForge.EVENT_BUS.post(new BlockGrowEvent(world, x, y, z, source, changes)))
        {
            HashSet<Point> restored = new HashSet<>();
            for(BlockSnapshot snapshot : changes)
            {
                Point snapPos = new Point(snapshot.x, snapshot.y, snapshot.z);
                if(restored.contains(snapPos))
                    continue;

                world.restoringBlockSnapshots = true;
                snapshot.restore(true, false);
                world.restoringBlockSnapshots = false;
                restored.add(snapPos);
            }
        }
        else
        {
            changes.stream().forEachOrdered(snapshot ->
                snapshot.world.markBlockForUpdate(snapshot.x, snapshot.y, snapshot.z)
            );
        }

        if(thrown != null)
            throw thrown;
    }

    @Referenced(at = SevenBlockDragonEggTransformer.class)
    public static void startCapturingBlocks(World world)
    {
        world.captureBlockSnapshots = true;
    }

    @Referenced(at = SevenBlockDragonEggTransformer.class)
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

    @Referenced(at = SevenEntityFishingHookTransformer.class)
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

    @Referenced(at = SevenEntityXPOrbTransformer.class)
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

    @Referenced(at = SevenEntityArrowTransformer.class)
    public static boolean onPlayerPickupArrow(EntityArrow arrow, EntityPlayer player)
    {
        Event event = new PlayerPickupArrowEvent(player, arrow);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = SevenEntityIgnitionTransformer.class)
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

    @Referenced(at = SevenEntityEnderCrystalTransformer.class)
    public static boolean onEntityDamage(Entity entity, DamageSource source, float amount)
    {
        EntityDamageEvent event = new EntityDamageEvent(entity, source, amount);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = SevenBlockTNTTransformer.class)
    public static boolean onArrowIgnite(World world, int x, int y, int z, Block block, EntityArrow arrow)
    {
        ProjectileModifyBlockEvent event = new ProjectileModifyBlockEvent(arrow, world, x, y, z, block);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = SevenEntityBoatTransformer.class)
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

    @Referenced(at = SevenEntityFishingHookTransformer.class)
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

    @Referenced(at = SevenEntityFishingHookTransformer.class)
    public static boolean onFishingHookBringEntity(EntityFishHook hook)
    {
        FishingHookBringEntityEvent event = new FishingHookBringEntityEvent(hook.field_146043_c, hook);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    private MineCitySevenHooks(){}
}
