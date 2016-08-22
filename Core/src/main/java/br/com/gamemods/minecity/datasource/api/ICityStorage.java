package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.permission.Group;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.OptionalPlayer;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.IslandArea;
import br.com.gamemods.minecity.structure.Plot;
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
    @Slow
    void setOwner(@NotNull City city, @NotNull OptionalPlayer owner)
            throws DataSourceException, IllegalStateException;

    @Slow
    void setSpawn(@NotNull City city, @NotNull BlockPos spawn)
            throws DataSourceException, IllegalStateException;

    @Slow
    @NotNull
    Island createIsland(@NotNull City city, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException;

    @Slow
    void claim(@NotNull Island island, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException, ClassCastException;

    @Slow
    @NotNull
    Island claim(@NotNull Set<Island> islands, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException;

    @Slow
    void deleteIsland(@NotNull Island island)
            throws DataSourceException, IllegalArgumentException, ClassCastException;

    @Slow
    void disclaim(@NotNull ChunkPos chunk, @NotNull Island island)
            throws DataSourceException, IllegalArgumentException;

    @Slow
    @NotNull
    Collection<Island> disclaim(@NotNull ChunkPos chunk, @NotNull Island island, @NotNull Set<Set<ChunkPos>> groups)
            throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException, IllegalArgumentException;

    @Slow
    void setName(@NotNull City city, @NotNull String identity, @NotNull String name) throws DataSourceException;

    @Slow
    void setName(@NotNull Group group, @NotNull String identity, @NotNull String name) throws DataSourceException;

    @Slow
    @NotNull
    Collection<ChunkPos> reserve(@NotNull IslandArea reserve) throws DataSourceException;

    @Slow
    void addMember(@NotNull Group group, @NotNull Identity<?> member) throws DataSourceException, UnsupportedOperationException;

    @Slow
    void removeMember(@NotNull Group group, @NotNull Identity<?> member) throws DataSourceException, UnsupportedOperationException;

    @Slow
    void addManager(@NotNull Group group, @NotNull PlayerID manager) throws DataSourceException;

    @Slow
    void removeManager(@NotNull Group group, @NotNull PlayerID manager) throws DataSourceException;

    @Slow
    void deleteGroup(@NotNull Group group) throws DataSourceException;

    @Slow
    @NotNull
    Collection<Group> loadGroups(@NotNull City city) throws DataSourceException;

    @Slow
    @NotNull
    Group createGroup(@NotNull City city, @NotNull String id, @NotNull String name) throws DataSourceException;

    @Slow
    void deleteCity(@NotNull City city) throws DataSourceException;

    @Slow
    int createPlot(Plot plot) throws DataSourceException;

    @Slow
    void setOwner(@NotNull Plot plot, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException;

    @Slow
    void setShape(@NotNull Plot plot, @NotNull Shape shape, BlockPos spawn) throws DataSourceException;

    @Slow
    void setName(@NotNull Plot plot, @NotNull String identity, @NotNull String name) throws DataSourceException;

    @Slow
    void setSpawn(@NotNull Plot plot, @NotNull BlockPos spawn) throws DataSourceException;

    @Slow
    void deletePlot(@NotNull Plot plot) throws DataSourceException;

    @Slow
    @NotNull
    Set<Plot> loadPlots(@NotNull Island island) throws DataSourceException;
}
