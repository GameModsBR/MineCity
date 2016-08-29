package br.com.gamemods.minecity.forge.mc_1_10_2.protection;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.*;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@Referenced
public class MineCityFrostHooks
{
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
