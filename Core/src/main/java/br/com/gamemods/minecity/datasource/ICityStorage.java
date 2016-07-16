package br.com.gamemods.minecity.datasource;

import br.com.gamemods.minecity.PlayerID;
import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICityStorage
{
    void setOwner(@NotNull City city, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException;

    void setSpawn(@NotNull City city, @NotNull BlockPos spawn) throws DataSourceException, IllegalStateException;
}
