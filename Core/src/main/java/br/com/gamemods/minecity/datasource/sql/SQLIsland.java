package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.WorldDim;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

final class SQLIsland implements Island
{
    int id, sizeX, sizeZ, chunkCount;
    @NotNull
    WorldDim world;

    City city;

    SQLIsland(int id, int sizeX, int sizeZ, int chunkCount, @NotNull WorldDim world)
    {
        this.id = id;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.chunkCount = chunkCount;
        this.world = world;
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

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        SQLIsland sqlIsland = (SQLIsland) o;
        return id == sqlIsland.id;

    }

    @Override
    public int hashCode()
    {
        return id;
    }
}
