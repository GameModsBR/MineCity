package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface IDataSource
{
    @Nullable
    ClaimedChunk getCityChunk(@NotNull ChunkPos pos) throws DataSourceException;

    @NotNull
    CityCreationResult createCity(@NotNull City city) throws DataSourceException, IllegalStateException;

    @Nullable
    String checkNameConflict(@NotNull String identityName) throws DataSourceException;

    void initDB() throws DataSourceException, IOException;

    void close() throws DataSourceException;

    @Nullable
    City getCityByName(@NotNull String name) throws DataSourceException;
}
