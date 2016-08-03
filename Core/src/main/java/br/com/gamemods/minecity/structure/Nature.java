package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

public final class Nature extends SimpleFlagHolder implements ChunkOwner
{
    @NotNull
    public final MineCity mineCity;
    @NotNull
    public final WorldDim world;
    private boolean valid = true;

    public Nature(@NotNull MineCity mineCity, @NotNull WorldDim world)
    {
        this.mineCity = mineCity;
        this.world = world;
        denyAll(mineCity.defaultNatureFlags);
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
