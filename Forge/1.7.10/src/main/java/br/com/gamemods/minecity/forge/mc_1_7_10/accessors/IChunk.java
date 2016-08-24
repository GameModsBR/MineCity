package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import net.minecraft.world.World;

public interface IChunk extends br.com.gamemods.minecity.forge.base.accessors.IChunk
{
    default World getWorld()
    {
        return getForgeChunk().worldObj;
    }

    @Override
    default IWorldServer getForgeWorldServer()
    {
        return (IWorldServer) getForgeChunk().worldObj;
    }
}
