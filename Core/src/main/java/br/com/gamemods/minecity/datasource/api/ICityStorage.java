package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Set;

public interface ICityStorage
{
    void setOwner(@NotNull City city, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException;

    void setSpawn(@NotNull City city, @NotNull BlockPos spawn) throws DataSourceException, IllegalStateException;

    @NotNull
    Island createIsland(@NotNull City city, @NotNull ChunkPos chunk) throws DataSourceException, IllegalStateException;

    void claim(@NotNull Island island, @NotNull ChunkPos chunk) throws DataSourceException, IllegalStateException;

    @NotNull
    Island claim(@NotNull Set<Island> islands, @NotNull ChunkPos chunk) throws DataSourceException, IllegalStateException, NoSuchElementException;
}
