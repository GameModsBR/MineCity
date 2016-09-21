package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityParticleTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.ExplosionIC2Transformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.TileEntityCropTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.TileEntityTeslaTransformer;
import br.com.gamemods.minecity.forge.base.protection.ShooterDamageSource;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Referenced
public class ICHooks
{
    private static Method getBaseSeed;
    private static Object crops;
    private static Method hasCompleteHazmat;

    public static boolean hasCompleteHazmat(IEntityLivingBase entity)
    {
        try
        {
            if(hasCompleteHazmat == null)
                hasCompleteHazmat = Class.forName("ic2.core.item.armor.ItemArmorHazmat").getDeclaredMethod("hasCompleteHazmat", EntityLivingBase.class);

            return (boolean) hasCompleteHazmat.invoke(null, entity);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static Object getBaseSeed(IItemStack stack)
    {
        try
        {
            if(getBaseSeed == null)
            {
                Class<?> c = Class.forName("ic2.api.crops.Crops");
                crops = c.getDeclaredField("instance").get(null);
                getBaseSeed = c.getDeclaredMethod("getBaseSeed", ItemStack.class);
            }

            return getBaseSeed.invoke(crops, stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = TileEntityCropTransformer.class)
    public static boolean onEntityTrample(ITileEntity tile, IEntity entity)
    {
        return !entity.isRemote() &&
                ModEnv.entityProtections.onEntityTrample(entity, tile.getIWorld(), tile.getPosX(), tile.getPosY(),
                        tile.getPosZ()
                );

    }

    @Referenced(at = EntityParticleTransformer.class)
    public static boolean onEntityBreakBlock(Entity mcEntity, World mcWorld, Point point)
    {
        IEntity entity = (IEntity) mcEntity;
        IWorldServer world = (IWorldServer) mcWorld;

        List<Permissible> relatives = ModEnv.entityProtections.getRelatives(entity);
        MineCityForge mod = ModEnv.entityProtections.mod;
        ForgePlayer player = relatives.stream().filter(EntityProtections.FILTER_PLAYER).findFirst()
                .map(perm -> mod.playerOrFake(perm, world))
                .map(mod::player).orElse(null);

        if(player == null)
            return true;

        IState state = world.getIState(point);
        Optional<Message> denial = state.getIBlock()
                .reactBlockBreak(player, state, point.toBlock(mod.world(world)))
                .can(mod.mineCity, player);

        if(denial.isPresent())
        {
            player.sendProjectileDenial(denial.get());
            return true;
        }

        return false;
    }

    @Referenced(at = ExplosionIC2Transformer.class)
    public static List<IEntity> onExplosionDamage(List<IEntity> entities, IExplosionIC2 explosion)
    {
        if(entities.isEmpty())
            return entities;

        IEntity exploder = explosion.getExploder();
        IEntityLivingBase igniter = explosion.getIgniter();
        if(exploder == null)
        {
            exploder = (IEntity) new EntityTNTPrimed((World) explosion.getWorld(),
                    explosion.getExplosionX(), explosion.getExplosionY(), explosion.getExplosionZ(),
                    (EntityLivingBase) igniter
            );
        }

        DamageSource source = new EntityDamageSourceIndirect("explosion.ic2", (Entity) exploder, igniter == null? (Entity)exploder : (Entity)igniter);
        entities.removeIf(entity -> ModEnv.entityProtections.onEntityDamage(entity, source, 10, true));
        return entities;
    }

    @Referenced(at = TileEntityTeslaTransformer.class)
    public static List<IEntity> onTeslaDamage(List<IEntity> entities, ITileEntity tesla)
    {
        if(entities.isEmpty())
            return entities;

        MineCityForge mod = ModEnv.blockProtections.mod;
        BlockPos teslaPos = tesla.getBlockPos(mod);
        Identity<?> owner = mod.mineCity.provideChunk(teslaPos.getChunk()).getFlagHolder(teslaPos).owner();
        DamageSource source = new ShooterDamageSource("bolt", new ProjectileShooter(teslaPos.toEntity(), owner));
        entities.removeIf(entity ->
                owner.getType() == Identity.Type.NATURE && entity.getPlayerAttackType() == PermissionFlag.PVP
                || ModEnv.entityProtections.onEntityDamage(entity, source, 20, true)
        );
        return entities;
    }
}
