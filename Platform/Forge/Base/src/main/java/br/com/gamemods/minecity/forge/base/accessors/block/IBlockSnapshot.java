package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import net.minecraftforge.common.util.BlockSnapshot;

public interface IBlockSnapshot
{
    default BlockSnapshot getBlockSnapshot()
    {
        return (BlockSnapshot) this;
    }

    default BlockPos getPosition(MineCityForge mod)
    {
        return new BlockPos(mod.world(getIWorld()), getX(), getY(), getZ());
    }

    default BlockPos getPosition(MineCityForge mod, BlockPos last)
    {
        WorldDim world = mod.world(getIWorld());

        if(last != null && last.world.equals(world))
            return new BlockPos(last, getX(), getY(), getZ());

        return new BlockPos(world, getX(), getY(), getZ());
    }

    int getX();
    int getY();
    int getZ();

    IWorldServer getIWorld();
    IState getCurrentState();
    IState getReplacedState();

    default ITileEntity getCurrentTileEntity()
    {
        return getIWorld().getTileEntity(getX(), getY(), getZ());
    }
}
