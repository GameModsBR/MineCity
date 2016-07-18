package br.com.gamemods.minecity.datasource.test;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

public class DummyIsland implements Island
{
    public int id;
    public WorldDim world;
    public City city;
    public int sizeX;
    public int sizeZ;
    public int chunkCount;

    public DummyIsland(WorldDim world, City city)
    {
        this.world = world;
        this.city = city;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @NotNull
    @Override
    public WorldDim getWorld()
    {
        return world;
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
