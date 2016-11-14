package br.com.gamemods.minecity.datasource.test;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.IExceptPermissionStorage;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class DummyIsland extends Island
{
    public int sizeX;
    public int sizeZ;
    public int chunkCount;

    public DummyIsland(ICityStorage storage, IExceptPermissionStorage permissionStorage, WorldDim world, City city)
    {
        super(city, storage, permissionStorage, 0, world, Collections.emptySet());
    }

    @Override
    protected void setCity(@NotNull City city)
    {
        super.setCity(city);
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
