package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
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

    int getX();
    int getY();
    int getZ();

    IWorldServer getIWorld();
    IState getCurrentState();
    IState getReplacedState();
}
