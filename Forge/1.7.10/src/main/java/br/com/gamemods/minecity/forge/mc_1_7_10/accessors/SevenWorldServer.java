package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.SevenUtil;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenWorldServerTransformer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

@Referenced(at = SevenWorldServerTransformer.class)
public interface SevenWorldServer extends IWorldServer
{
    @Override
    default SevenBlock getIBlock(int x, int y, int z)
    {
        return (SevenBlock) ((WorldServer) this).getBlock(x, y, z);
    }

    @Override
    default SevenBlock getIState(int x, int y, int z)
    {
        return getIBlock(x, y, z);
    }

    @Override
    default boolean isSideSolid(int x, int y, int z, Direction direction)
    {
        return ((WorldServer) this).isSideSolid(x, y, z, SevenUtil.toForgeDirection(direction));
    }

    default boolean isTopSolid(int x, int y, int z)
    {
        return ((WorldServer) this).isSideSolid(x, y, z, ForgeDirection.UP);
    }

    @Override
    default int getDimensionId()
    {
        return ((WorldServer) this).provider.dimensionId;
    }
}
