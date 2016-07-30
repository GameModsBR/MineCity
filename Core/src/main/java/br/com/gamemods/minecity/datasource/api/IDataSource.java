package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IDataSource
{
    @Slow
    @Nullable
    ClaimedChunk getCityChunk(@NotNull ChunkPos pos) throws DataSourceException;

    @Slow
    @NotNull
    CityCreationResult createCity(@NotNull City city) throws DataSourceException, IllegalStateException;

    @Nullable
    String checkNameConflict(@NotNull String identityName);

    @Slow
    void initDB() throws DataSourceException, IOException;

    @Slow
    void close() throws DataSourceException;

    @Slow
    @NotNull
    Optional<City> getCityByName(@NotNull String name) throws DataSourceException;

    @Slow
    @NotNull
    Optional<PlayerID> getPlayer(@NotNull String name) throws DataSourceException;

    Supplier<Stream<String>> cityNameSupplier();
}
