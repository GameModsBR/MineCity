package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.api.shape.PreciseCuboid;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.FrostUtil;
import br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block.FrostBlock;
import br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block.FrostState;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostWorldServerTransformer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.BlockSnapshot;

import java.util.List;
import java.util.stream.Collectors;

@Referenced(at = FrostWorldServerTransformer.class)
public interface FrostWorldServer extends IWorldServer
{
    @Override
    default FrostBlock getIBlock(int x, int y, int z)
    {
        return getIState(x, y, z).getIBlock();
    }

    @Override
    default FrostState getIState(int x, int y, int z)
    {
        return (FrostState) ((WorldServer) this).getBlockState(new BlockPos(x, y, z));
    }

    default boolean isSideSolid(int x, int y, int z, Direction direction)
    {
        EnumFacing side = FrostUtil.toFace(direction);
        if(side == null)
            return getIState(x, y, z).isOpaqueCube();

        return ((WorldServer) this).isSideSolid(new BlockPos(x, y, z), side);
    }

    default boolean isTopSolid(int x, int y, int z)
    {
        return ((WorldServer) this).isSideSolid(new BlockPos(x, y, z), EnumFacing.UP);
    }

    @Override
    default boolean setBlock(int x, int y, int z, IState state)
    {
        return ((WorldServer) this).setBlockState(new BlockPos(x, y, z), (IBlockState) state);
    }

    @Override
    default IRayTraceResult rayTraceBlocks(PrecisePoint start, PrecisePoint end, boolean stopOnLiquid)
    {
        return (IRayTraceResult) ((WorldServer) this).rayTraceBlocks(
                new Vec3d(start.x, start.y, start.z),
                new Vec3d(end.x, end.y, end.z),
                stopOnLiquid
        );
    }

    @Override
    default List<PreciseCuboid> getCollisionBoxes(PreciseCuboid cuboid)
    {
        AxisAlignedBB box = new AxisAlignedBB(
                cuboid.min.x,
                cuboid.min.y,
                cuboid.min.z,
                cuboid.max.x,
                cuboid.max.y,
                cuboid.max.z
        );

        return ((WorldServer) this).getCollisionBoxes(box).stream().map(bb->
                new PreciseCuboid(
                        new PrecisePoint(bb.minX, bb.minY, bb.minZ),
                        new PrecisePoint(bb.maxX, bb.maxY, bb.maxZ)
                )
        ).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<IEntity> getEntities(PreciseCuboid cuboid)
    {
        AxisAlignedBB box = new AxisAlignedBB(
                cuboid.min.x,
                cuboid.min.y,
                cuboid.min.z,
                cuboid.max.x,
                cuboid.max.y,
                cuboid.max.z
        );

        return (List) ((WorldServer) this).getEntitiesWithinAABBExcludingEntity(null, box);
    }

    @Override
    default ITileEntity getTileEntity(int x, int y, int z)
    {
        return (ITileEntity) ((WorldServer) this).getTileEntity(new BlockPos(x, y, z));
    }

    @Override
    default boolean isAir(int x, int y, int z)
    {
        return ((WorldServer) this).isAirBlock(new BlockPos(x, y, z));
    }

    @Override
    default boolean isBlockLoaded(int x, int y, int z)
    {
        return ((WorldServer) this).isBlockLoaded(new BlockPos(x, y, z));
    }

    @Override
    default IBlockSnapshot getBlockSnapshot(int x, int y, int z)
    {
        return (IBlockSnapshot) BlockSnapshot.getBlockSnapshot((World)this, new BlockPos(x, y, z));
    }
}
