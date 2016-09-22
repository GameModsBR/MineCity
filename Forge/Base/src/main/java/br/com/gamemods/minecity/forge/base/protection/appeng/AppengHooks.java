package br.com.gamemods.minecity.forge.base.protection.appeng;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityArrow;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityTNTPrimed;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng.*;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

@Referenced
public class AppengHooks
{
    @Referenced(at = EntityTinyTNTPrimedTransformer.class)
    public static List<IEntity> onTinyTntDamage(List<IEntity> entities, IEntityTNTPrimed tnt)
    {
        if(entities.isEmpty())
            return entities;

        IEntityLivingBase placer = tnt.getPlacedBy();
        DamageSource source = new EntityDamageSourceIndirect("explosion.appeng", (Entity)tnt, placer == null? (Entity)tnt : (Entity)placer);
        entities.removeIf(entity -> ModEnv.entityProtections.onEntityDamage(entity, source, 10, true));
        return entities;
    }

    @Referenced(at = EntityTinyTNTPrimedTransformer.class)
    public static Block onTinyTntBreak(World world, int x, int y, int z, IEntityTNTPrimed tnt)
    {
        IBlock block = ((IWorldServer) world).getIBlock(x, y, z);
        if(block != null && !block.getUnlocalizedName().equals("tile.air"))
        {
            List<Permissible> relatives = ModEnv.entityProtections.getRelatives(tnt);
            Permissible who = relatives.stream().filter(EntityProtections.FILTER_PLAYER).findFirst()
                    .orElseGet(()-> relatives.stream().filter(p-> p.identity().getType() != Identity.Type.ENTITY).findFirst()
                            .orElse(tnt)
                    );

            MineCityForge mod = ModEnv.entityProtections.mod;
            if(mod.mineCity.provideChunk(new ChunkPos(mod.world(world), x>>4,z>>4)).getFlagHolder(x, y, z)
                    .can(who, PermissionFlag.MODIFY).isPresent())
            {
                return null;
            }
        }

        return (Block) block;
    }

    @Referenced(at = EntityTinyTNTPrimedTransformer.class)
    public static boolean onEntityCollideWithTinyTnt(World world, int x, int y, int z, Entity entity)
    {
        if(world.isRemote)
            return false;

        if(entity instanceof IEntityArrow)
        {
            IEntityArrow arrow = (IEntityArrow) entity;
            if(arrow.isBurning())
            {
                List<Permissible> relatives = ModEnv.entityProtections.getRelatives(arrow);
                Permissible who = relatives.stream().filter(EntityProtections.FILTER_PLAYER).findFirst()
                        .orElseGet(()-> relatives.stream().filter(p-> p.identity().getType() != Identity.Type.ENTITY).findFirst()
                                .orElse(arrow)
                        );

                MineCityForge mod = ModEnv.entityProtections.mod;
                return mod.mineCity.provideChunk(new ChunkPos(mod.world(world), x>>4,z>>4)).getFlagHolder(x, y, z)
                        .can(who, PermissionFlag.MODIFY).isPresent();
            }
        }

        return false;
    }

    @Referenced(at = WirelessTerminalGuiObjectTransformer.class)
    public static boolean onPlayerAccessWap(EntityPlayer player, World world, int x, int y, int z)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        return mod.mineCity.provideChunk(new ChunkPos(mod.world(world), x>>4, z>>4)).getFlagHolder(x, y, z)
               .can((IEntityPlayerMP) player, PermissionFlag.OPEN).isPresent();
    }

    @Referenced(at = ToolMassCannonTransformer.class)
    public static boolean onMassCannonHit(World world, EntityPlayer player, IRayTraceResult result, Enum<?> type)
    {
        int ord = type.ordinal();
        if(ord == 2) // ENTITY
        {
            DamageSource source = new EntityDamageSource("masscannon", player);
            return ModEnv.entityProtections.onEntityDamage(result.getEntity(), source, 2, false);
        }
        else if(ord == 1) // BLOCK
        {
            MineCityForge mod = ModEnv.blockProtections.mod;
            BlockPos pos = result.getHitBlockPos(mod.world(world));
            return mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can((IEntityPlayerMP) player, PermissionFlag.MODIFY).isPresent();
        }

        return false;
    }

    @Referenced(at = PartPlacementTransformer.class)
    public static boolean onPartPlace(ItemStack mcStack, int x, int y, int z, int mcFace, EntityPlayer mcPlayer, World mcWorld, int packetType, int depth)
    {
        if(mcWorld.isRemote || depth > 3)
            return false;

        MineCityForge mod = ModEnv.blockProtections.mod;
        Direction face = ForgeUtil.toDirection(mcFace);
        BlockPos pos = new BlockPos(mod.world(mcWorld), x, y, z);
        if(ModEnv.blockProtections.onPlayerRightClickBlock(mcPlayer, false, mcStack, ((IWorldServer)mcWorld).getIState(pos), pos, face, true) != 0)
        {
            ((IEntityPlayerMP) mcPlayer).sendBlockAndTile(x, y, z);
            return true;
        }

        return false;
    }

    @Referenced(at = PartAnnihilationPaneTransformer.class)
    public static boolean onPartModify(IAEBasePart part, WorldServer mcWorld, int x, int y, int z)
    {
        BlockPos pos = part.getHost().getPos(ModEnv.blockProtections.mod);
        return ModHooks.onBlockAccessOther(mcWorld, x, y, z, pos.x, pos.y, pos.z, PermissionFlag.MODIFY).isPresent();
    }
}
