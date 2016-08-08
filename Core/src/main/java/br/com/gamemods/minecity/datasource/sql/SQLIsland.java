package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Inconsistency;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

final class SQLIsland extends Island
{
    int minX;
    int maxX;
    int minZ;
    int maxZ;
    int chunkCount;

    @NotNull
    City city;

    SQLIsland(ICityStorage storage, int id, int minX, int maxX, int minZ, int maxZ, int chunkCount, @NotNull WorldDim world)
    {
        super(storage, id, world);
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.chunkCount = chunkCount;
        this.city = Inconsistency.getInconsistentCity();
    }

    SQLIsland(ICityStorage storage, int id, ChunkPos chunk, @NotNull City city)
    {
        super(storage, id, chunk.world);
        minX = maxX = chunk.x;
        minZ = maxZ = chunk.z;
        chunkCount = 1;
        this.city = city;
    }

    void add(ChunkPos chunk)
    {
        minX = Math.min(minX, chunk.x);
        maxX = Math.max(maxX, chunk.x);
        minZ = Math.min(minZ, chunk.z);
        maxZ = Math.max(maxZ, chunk.z);
        chunkCount++;
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
        return chunkCount == 0? 0 : maxX - minX + 1;
    }

    @Override
    public int getSizeZ()
    {
        return chunkCount == 0? 0 : maxZ - minZ + 1;
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

    @Override
    public String toString()
    {
        return "SQLIsland{" +
                "id=" + id +
                ", world=" + world +
                ", city=" + city +
                '}';
    }
}
