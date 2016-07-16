package br.com.gamemods.minecity.datasource;

import br.com.gamemods.minecity.structure.Island;

public final class CityCreationResult
{
    public final ICityStorage storage;
    public final Island island;

    public CityCreationResult(ICityStorage storage, Island island)
    {
        this.storage = storage;
        this.island = island;
    }
}
