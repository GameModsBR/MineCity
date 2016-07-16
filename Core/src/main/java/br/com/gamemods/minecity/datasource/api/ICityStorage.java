package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.structure.City;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICityStorage
{
    void setOwner(@NotNull City city, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException;

    void setSpawn(@NotNull City city, @NotNull BlockPos spawn) throws DataSourceException, IllegalStateException;
}
