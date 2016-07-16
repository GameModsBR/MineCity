package br.com.gamemods.minecity.datasource;

import br.com.gamemods.minecity.api.ChunkPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDataSource
{
    @Nullable
    ClaimedChunk getCityChunk(@NotNull ChunkPos pos) throws DataSourceException;

    @NotNull
    CityCreationResult createCity(@NotNull City city) throws DataSourceException, IllegalStateException;
}
