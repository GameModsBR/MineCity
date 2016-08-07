package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import org.jetbrains.annotations.NotNull;

public interface Island extends ChunkOwner
{
    int getId();

    @NotNull
    WorldDim getWorld();
    @NotNull
    City getCity();
    int getSizeX();
    int getSizeZ();
    int getChunkCount();
    default IslandArea getArea() throws DataSourceException
    {
        return getCity().mineCity.dataSource.getArea(this);
    }
}
