package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.IslandArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Internal city data management, these methods should only be called internally by {@link City}
 */
public interface ICityStorage
{
    void setOwner(@NotNull City city, @Nullable PlayerID owner)
            throws DataSourceException, IllegalStateException;

    void setSpawn(@NotNull City city, @NotNull BlockPos spawn)
            throws DataSourceException, IllegalStateException;

    @NotNull
    Island createIsland(@NotNull City city, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException;

    void claim(@NotNull Island island, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException, ClassCastException;

    @NotNull
    Island claim(@NotNull Set<Island> islands, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException;

    void deleteIsland(@NotNull Island island)
            throws DataSourceException, IllegalArgumentException, ClassCastException;

    void disclaim(@NotNull ChunkPos chunk, @NotNull Island island)
            throws DataSourceException, IllegalArgumentException;

    @NotNull
    Collection<Island> disclaim(@NotNull ChunkPos chunk, @NotNull Island island, @NotNull Set<Set<ChunkPos>> groups)
            throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException, IllegalArgumentException;

    @NotNull
    IslandArea getArea(@NotNull Island island) throws DataSourceException, ClassCastException, IllegalArgumentException;

    void setName(@NotNull City city, @NotNull String identity, @NotNull String name) throws DataSourceException;

    Collection<ChunkPos> reserve(@NotNull IslandArea reserve) throws DataSourceException;
}
