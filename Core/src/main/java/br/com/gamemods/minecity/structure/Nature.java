package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.WorldDim;

public class Nature implements ChunkOwner
{
    public final WorldDim world;
    private boolean valid = true;

    public Nature(WorldDim world)
    {
        this.world = world;
    }

    public void invalidate()
    {
        valid = false;
    }

    public boolean isValid()
    {
        return valid;
    }
}
