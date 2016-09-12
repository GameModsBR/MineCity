package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IPotionEffect;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemSeeds;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ChemthrowerEffectTeleportTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ChemthrowerHandlerTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.EntityChemthrowerShotTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ItemIEToolTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImmersiveHooks
{
    private static Field itemSeeds;
    private static Field itemMaterial;
    private static Field shootingEntity;
    private static Class<?> classIEContent;
    private static Method getDye;

    public static IEntityLivingBase getShootingEntity(IEntityRevolverShot shot)
    {
        try
        {
            if(shootingEntity == null)
                shootingEntity = Class.forName("blusunrize.immersiveengineering.common.entities.EntityRevolvershot").getDeclaredField("shootingEntity");
            return (IEntityLivingBase) shootingEntity.get(shot);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = ItemIEToolTransformer.class)
    public static boolean preCreateStructure(Object multiBlock, World world, int x, int y, int z, int side, EntityPlayer player)
    {
        world.captureBlockSnapshots = true;
        return false;
    }

    @Referenced(at = ItemIEToolTransformer.class)
    public static boolean postCreateStructure(boolean result, Object multiBlock, World world, int x, int y, int z, int side, EntityPlayer entityPlayer)
    {
        world.captureBlockSnapshots = false;
        @SuppressWarnings("unchecked")
        ArrayList<IBlockSnapshot> capture = new ArrayList(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        int size = capture.size();
        if(size == 0)
            return result;

        Reaction react = new MultiBlockReaction(PermissionFlag.MODIFY,
                capture.stream().map(snap-> snap.getPosition(ModEnv.entityProtections.mod))
                    .distinct().collect(Collectors.toList())
        );

        IEntityPlayerMP player = (IEntityPlayerMP) entityPlayer;
        ModEnv.entityProtections.mod.player(player);
        Optional<Message> denial = react.can(ModEnv.entityProtections.mod.mineCity, player);
        if(denial.isPresent())
        {
            MineCityForge.snapshotHandler.restore(capture);
            player.send(FlagHolder.wrapDeny(denial.get()));
            return result;
        }

        MineCityForge.snapshotHandler.send(capture);
        return result;
    }

    public static int getDye(ItemStack stack)
    {
        try
        {
            if(getDye == null)
                getDye = Class.forName("blusunrize.immersiveengineering.common.util.Utils").getDeclaredMethod("getDye",
                        ItemStack.class
                );

            return (int) getDye.invoke(null, stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = ChemthrowerEffectTeleportTransformer.class)
    public static EnderTeleportEvent onChemthrowerTeleport(EnderTeleportEvent event, IEntity attacked, double x, double y, double z, IChemthrowerEffect effect, EntityPlayer shooter)
    {
        if(shooter == null)
        {
            event.setCanceled(true);
            return event;
        }

        IEntityPlayerMP attacker = (IEntityPlayerMP) shooter;
        MineCityForge mod = ModEnv.entityProtections.mod;
        mod.player(attacker);

        if(ModEnv.entityProtections.onEntityDamage(attacked, new EntityDamageSourceIndirect("teleport", shooter, shooter), 1f))
        {
            event.setCanceled(true);
            return event;
        }

        BlockPos from = attacked.getBlockPos(mod);
        ClaimedChunk claim = mod.mineCity.provideChunk(from.getChunk());
        FlagHolder fromHolder = claim.getFlagHolder(from);

        BlockPos to = new BlockPos(from, (int)x, (int)y, (int)z);
        FlagHolder toHolder = mod.mineCity.provideChunk(to.getChunk(), claim).getFlagHolder(to);

        if(fromHolder != toHolder)
        {
            if(attacked.getType() == MinecraftEntity.Type.PLAYER)
            {
                if(toHolder.can(attacked, PermissionFlag.ENTER).isPresent())
                    event.setCanceled(true);
            }
            else
            {
                if(FlagHolder.can((IEntity) shooter, PermissionFlag.MODIFY, fromHolder, toHolder).findFirst().isPresent())
                    event.setCanceled(true);
            }
        }

        return event;
    }

    @Referenced(at = ChemthrowerHandlerTransformer.class)
    public static void onPotionApplyEffect(EntityLivingBase target, PotionEffect effect, IChemthrowerEffect chemEffect, EntityPlayer shooter)
    {
        if(shooter != null && !ModEnv.entityProtections.onPotionApply(
                (IEntityLivingBase) target,
                (IPotionEffect) effect,
                (IEntity) shooter
        ))
        {
            target.addPotionEffect(effect);
        }
    }

    @Referenced(at = ChemthrowerHandlerTransformer.class)
    public static boolean onChemthrowerDamage(Entity entity, DamageSource source, float damage, IChemthrowerEffect effect, EntityPlayer shooter)
    {
        return shooter != null
                && !ModEnv.entityProtections.onEntityDamage(
                        (IEntity) entity,
                        new EntityDamageSourceIndirect(source.damageType, shooter, shooter).setProjectile(),
                        damage
                    )
                && entity.attackEntityFrom(source, damage)
        ;
    }

    @Referenced(at = EntityChemthrowerShotTransformer.class)
    public static boolean onChemthrowerDamage(Entity entity, DamageSource source, float damage, EntityArrow shot)
    {
        EntityDamageSourceIndirect indirect = new EntityDamageSourceIndirect(source.damageType, shot, shot.shootingEntity == null? shot : shot.shootingEntity);
        return entity.attackEntityFrom(indirect.setFireDamage().setProjectile(), damage);
    }

    private static Class<?> getIEContent() throws ReflectiveOperationException
    {
        if(classIEContent == null)
            classIEContent = Class.forName("blusunrize.immersiveengineering.common.IEContent");
        return classIEContent;
    }

    public static IItemSeeds getItemSeeds()
    {
        try
        {
            if(itemSeeds == null)
                itemSeeds = getIEContent().getDeclaredField("itemSeeds");
            return (IItemSeeds) itemSeeds.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static IItemSeeds getItemMaterial()
    {
        try
        {
            if(itemMaterial == null)
                itemMaterial = getIEContent().getDeclaredField("itemMaterial");
            return (IItemSeeds) itemMaterial.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }
}
