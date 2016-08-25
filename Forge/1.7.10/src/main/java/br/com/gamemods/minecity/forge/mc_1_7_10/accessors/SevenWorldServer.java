package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenWorldServerTransformer;
import net.minecraft.world.WorldServer;

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
}
