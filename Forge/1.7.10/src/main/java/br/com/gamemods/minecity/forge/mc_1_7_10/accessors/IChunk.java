package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;

public interface IChunk extends br.com.gamemods.minecity.forge.base.accessors.IChunk
{
    @Override
    default IWorldServer getForgeWorldServer()
    {
        return (IWorldServer) getForgeChunk().worldObj;
    }
}
