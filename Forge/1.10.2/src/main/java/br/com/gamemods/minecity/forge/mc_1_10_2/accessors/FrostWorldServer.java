package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_10_2.FrostUtil;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostWorldServerTransformer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

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
}
