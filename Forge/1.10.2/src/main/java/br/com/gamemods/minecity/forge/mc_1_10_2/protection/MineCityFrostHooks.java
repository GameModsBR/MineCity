package br.com.gamemods.minecity.forge.mc_1_10_2.protection;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.*;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.*;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

@Referenced
public class MineCityFrostHooks
{
    @Referenced(at = FrostEntityEggTransformer.class)
    public static boolean onEggSpawnChicken(EntityEgg egg)
    {
        return MinecraftForge.EVENT_BUS.post(new EggSpawnChickenEvent(egg));
    }

    @Contract("!null, _, _, _, _ -> fail")
    @Referenced(at = FrostGrowMonitorTransformer.class)
    public static void onGrowableGrow(Throwable thrown, Object source, World world, BlockPos pos, IBlockState state)
            throws Throwable
    {
        world.captureBlockSnapshots = false;
        if(world.capturedBlockSnapshots.isEmpty())
        {
            if(thrown != null)
                throw thrown;
            return;
        }

        ArrayList<BlockSnapshot> changes = new ArrayList<>(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        if(MinecraftForge.EVENT_BUS.post(new BlockGrowEvent(world, pos, state, source, changes)))
        {
            HashSet<BlockPos> restored = new HashSet<>();
            for(BlockSnapshot snapshot : changes)
            {
                BlockPos snapPos = snapshot.getPos();
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
            HashSet<BlockPos> notified = new HashSet<>();
            for(BlockSnapshot snapshot : changes)
            {
                BlockPos snapPos = snapshot.getPos();
                if(notified.contains(snapPos))
                    continue;

                world.notifyBlockUpdate(snapPos, snapshot.getReplacedBlock(), snapshot.getCurrentBlock(), snapshot.getFlag());
                notified.add(snapPos);
            }
        }

        if(thrown != null)
            throw thrown;
    }

    @Referenced(at = FrostBlockDragonEggTransformer.class)
    public static void startCapturingBlocks(World world)
    {
        world.captureBlockSnapshots = true;
    }

    @Referenced(at = FrostBlockDragonEggTransformer.class)
    public static void onDragonEggTeleport(BlockDragonEgg block, EntityPlayer player, World world, BlockPos pos, IBlockState state)
    {
        world.captureBlockSnapshots = false;
        ArrayList<BlockSnapshot> changes = new ArrayList<>(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        if(MinecraftForge.EVENT_BUS.post(new PlayerTeleportDragonEggEvent(player, world, pos, state, changes)))
            changes.forEach(snapshot -> {
                world.restoringBlockSnapshots = true;
                snapshot.restore(true, false);
                world.restoringBlockSnapshots = false;
            });
    }

    @Referenced(at = FrostEntityFishingHookTransformer.class)
    public static Entity onFishingHookSpawnEntity(Entity entity, EntityFishHook hook)
    {
        MinecraftForge.EVENT_BUS.post(new EntitySpawnByFishingHookEvent(entity, hook));
        return entity;
    }

    @Referenced(at = FrostEntityXPOrbTransformer.class)
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

    @Referenced(at = FrostEntityArrowTransformer.class)
    public static boolean onPlayerPickupArrow(EntityArrow arrow, EntityPlayer player)
    {
        Event event = new PlayerPickupArrowEvent(player, arrow);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = FrostEntityIgnitionTransformer.class)
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

    @Referenced(at = FrostEntityEnderCrystalTransformer.class)
    public static boolean onEntityDamage(Entity entity, DamageSource source, float amount)
    {
        EntityDamageEvent event = new EntityDamageEvent(entity, source, amount);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = FrostBlockTNTTransformer.class)
    public static boolean onArrowIgnite(World world, BlockPos pos, IBlockState state, EntityArrow arrow)
    {
        ProjectileModifyBlockEvent event = new ProjectileModifyBlockEvent(arrow, world, pos, state);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    /**
     * @deprecated {@link PlayerInteractEvent.EntityInteractSpecific} does basically the same thing but has better support for modded entities
     */
    @Deprecated
    @Nullable
    @Referenced(at = FrostEntityArmorStandTransformer.class)
    public static EnumActionResult onPrecisePlayerInteraction(Entity entity, EntityPlayer player, Vec3d vec, ItemStack stack, EnumHand hand)
    {
        PlayerInteractEntityPreciseEvent event = new PlayerInteractEntityPreciseEvent(entity, player, vec, stack, hand);
        if(MinecraftForge.EVENT_BUS.post(event))
            return EnumActionResult.FAIL;
        else
            return null;
    }

    @Referenced(at = FrostEntityBoatTransformer.class)
    public static boolean onVehicleDamage(Entity entity, DamageSource source, float amount)
    {
        VehicleDamageEvent event = new VehicleDamageEvent(entity, source, amount);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = FrostEntityPotionTransformer.class)
    public static void onPotionApplyEffect(EntityLivingBase entity, PotionEffect effect, Entity potion)
    {
        PotionApplyEvent event = new PotionApplyEvent(entity, effect, potion);
        if(!MinecraftForge.EVENT_BUS.post(event))
            entity.addPotionEffect(effect);
    }

    @Contract("null, _ -> null")
    @Referenced(at = FrostEntityFishingHookTransformer.class)
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

    @Referenced(at = FrostEntityFishingHookTransformer.class)
    public static boolean onFishingHookBringEntity(EntityFishHook hook)
    {
        FishingHookBringEntityEvent event = new FishingHookBringEntityEvent(hook.caughtEntity, hook);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    private MineCityFrostHooks(){}
}
