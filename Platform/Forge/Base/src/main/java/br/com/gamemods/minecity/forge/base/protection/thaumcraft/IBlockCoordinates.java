package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft.BlockCoordinatesTransformer;

@Referenced(at = BlockCoordinatesTransformer.class)
public interface IBlockCoordinates
{
    @Referenced(at = BlockCoordinatesTransformer.class)
    int getX();

    @Referenced(at = BlockCoordinatesTransformer.class)
    int getY();

    @Referenced(at = BlockCoordinatesTransformer.class)
    int getZ();

    default Point toPoint()
    {
        return new Point(getX(), getY(), getZ());
    }

    default BlockPos toPos(WorldDim dim)
    {
        return new BlockPos(dim, getX(), getY(), getZ());
    }

    default BlockPos toPos(WorldDim dim, BlockPos cache)
    {
        return new BlockPos(cache, dim, getX(), getY(), getZ());
    }
}
