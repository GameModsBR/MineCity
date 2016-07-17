package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.WorldDim;

public class Nature implements ChunkOwner
{
    public final WorldDim world;

    public Nature(WorldDim world)
    {
        this.world = world;
    }
}
