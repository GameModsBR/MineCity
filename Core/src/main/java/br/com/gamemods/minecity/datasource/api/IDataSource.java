package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.IslandArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    @NotNull
    Supplier<Stream<String>> cityNameSupplier();

    @NotNull
    Optional<Set<String>> getGroupNames(@NotNull String cityName);

    @NotNull
    Map<String, Set<String>> getGroups();

    @Slow
    @NotNull
    Set<GroupID> getEntityGroups(Identity<?> identity) throws DataSourceException;

    @Slow
    @NotNull
    IslandArea getArea(@NotNull Island island) throws DataSourceException, ClassCastException, IllegalArgumentException;
}
