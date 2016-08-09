package br.com.gamemods.minecity.datasource.test;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class DummyIsland extends Island
{
    public City city;
    public int sizeX;
    public int sizeZ;
    public int chunkCount;

    public DummyIsland(ICityStorage storage, WorldDim world, City city)
    {
        super(storage, 0, world, Collections.emptySet());
        this.city = city;
    }

    @NotNull
    @Override
    public City getCity()
    {
        return city;
    }

    @Override
    public int getSizeX()
    {
        return sizeX;
    }

    @Override
    public int getSizeZ()
    {
        return sizeZ;
    }

    @Override
    public int getChunkCount()
    {
        return chunkCount;
    }
}
