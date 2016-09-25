package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.*;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import br.com.gamemods.minecity.forge.base.protection.ShooterDamageSource;
import br.com.gamemods.minecity.forge.base.protection.reaction.RevertDeniedReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Referenced
public class ICHooks
{
    private static ITileEntity terraforming;
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

    @Referenced(at = TileEntityCropmatronTransformer.class)
    public static ITileEntity onTileAccessOther(ITileEntity accessed, ITileEntity from)
    {
        if(accessed == null)
            return null;

        Optional<Message> denial = ModHooks.onBlockAccessOther(
                (World) accessed.getIWorld(),
                accessed.getPosX(), accessed.getPosY(), accessed.getPosZ(),
                from.getPosX(), from.getPosY(), from.getPosZ(),
                PermissionFlag.MODIFY
        );

        if(denial.isPresent())
            return null;

        return accessed;
    }

    @Referenced(at = TileEntityMinerTransformer.class)
    public static boolean onMinerModify(ITileEntity tile, int x, int y, int z)
    {
        return !ModHooks.onBlockAccessOther
                ((World) tile.getIWorld(), x, y, z,
                tile.getPosX(), tile.getPosY(), tile.getPosZ(),
                PermissionFlag.MODIFY
        ).isPresent();
    }

    @Referenced(at = TileEntityTerraTransformer.class)
    public static boolean onTerraformStart(TileEntity tile, World world, Point point)
    {
        ITileEntity te = (ITileEntity) tile;
        MineCityForge mod = ModEnv.blockProtections.mod;
        BlockPos tilePos = te.getBlockPos(mod);
        if(new SingleBlockReaction(point.toBlock(mod.world(world)), PermissionFlag.MODIFY)
                .can(mod.mineCity,mod.mineCity.provideChunk(tilePos.getChunk()).getFlagHolder(tilePos).owner())
                .isPresent())
        {
            return false;
        }

        terraforming = te;
        world.captureBlockSnapshots = true;
        return false;
    }

    @Referenced(at = TileEntityTerraTransformer.class)
    public static boolean onTerraformEnds(boolean success, TileEntity tile, World world)
    {
        world.captureBlockSnapshots = false;
        terraforming = null;
        @SuppressWarnings("unchecked")
        List<IBlockSnapshot> captured = new ArrayList(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        if(captured.isEmpty())
            return success;

        MineCityForge mod = ModEnv.blockProtections.mod;
        BlockPos pos = ((ITileEntity) tile).getBlockPos(mod);
        return !new RevertDeniedReaction(mod, captured, PermissionFlag.MODIFY)
                .addAllowListener((reaction, permissible, flag, p, message) -> MineCityForge.snapshotHandler.send(p))
                .can(mod.mineCity, mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).owner())
                .isPresent() && success;
    }

    @Referenced(at = BiomeUtilTransformer.class)
    public static boolean onChangeBiome(World world, Point point)
    {
        return onChangeBiome(world, point.x>>4, point.z>>4);
    }

    @Referenced(at = TileEntityTerraTransformer.class)
    public static boolean onChangeBiome(World world, int x, int z)
    {
        ITileEntity tile = ICHooks.terraforming;
        return tile != null && ModHooks.onTileEntityChangeBiome(tile, (IWorldServer) world, x, z).isPresent();
    }

    @Referenced(at = TileEntityTeleporterTransformer.class)
    public static boolean onTeleport(ITileEntityTeleporter teleporter, IEntity user)
    {
        NBTTagCompound data = ((Entity) user).getEntityData();
        byte cooldown = data.getByte("MC$TPCooldown");
        if(cooldown > 0)
        {
            data.setByte("MC$TPCooldown", --cooldown);
            return true;
        }

        MineCityForge mod = ModEnv.entityProtections.mod;
        BlockPos target = teleporter.getTarget(mod);
        if(target == null)
            return false;

        Optional<Message> denial;
        if(user instanceof IEntityPlayerMP)
        {
            mod.player((IEntityPlayerMP) user);
            denial = mod.mineCity.provideChunk(target.getChunk())
                    .getFlagHolder(target).can(user,PermissionFlag.ENTER);
        }
        else
        {
            ClaimedChunk targetClaim = mod.mineCity.provideChunk(target.getChunk());
            FlagHolder flagHolder = targetClaim.getFlagHolder(target);
            BlockPos from = user.getBlockPos(target);
            Identity<?> owner = mod.mineCity.provideChunk(from.getChunk(), targetClaim).getFlagHolder(from).owner();
            denial = flagHolder.can(owner, PermissionFlag.MODIFY);
        }

        if(denial.isPresent())
        {
            data.setByte("MC$TPCooldown", (byte) 60);
            user.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }
        else if(cooldown > 0)
        {
            data.setByte("MC$TPCooldown", (byte) 0);
        }

        return false;
    }
}
