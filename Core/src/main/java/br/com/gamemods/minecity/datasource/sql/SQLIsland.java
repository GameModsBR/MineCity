package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

final class SQLIsland implements Island
{
    int id, minX, maxX, minZ, maxZ, chunkCount;
    @NotNull
    WorldDim world;

    @SuppressWarnings("NullableProblems")
    @NotNull
    City city;

    SQLIsland(int id, int minX, int maxX, int minZ, int maxZ, int chunkCount, @NotNull WorldDim world)
    {
        this.id = id;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.chunkCount = chunkCount;
        this.world = world;
    }

    SQLIsland(int id, ChunkPos chunk)
    {
        this.id = id;
        minX = maxX = chunk.x;
        minZ = maxZ = chunk.z;
        world = chunk.world;
        chunkCount = 1;
    }

    void add(ChunkPos chunk)
    {
        minX = Math.min(minX, chunk.x);
        maxX = Math.max(maxX, chunk.x);
        minZ = Math.min(minZ, chunk.z);
        maxZ = Math.max(maxZ, chunk.z);
        chunkCount++;
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
        return maxX - minX + 1;
    }

    @Override
    public int getSizeZ()
    {
        return maxZ - minZ + 1;
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
