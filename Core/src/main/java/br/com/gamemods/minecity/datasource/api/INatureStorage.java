package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.structure.Nature;
import org.jetbrains.annotations.NotNull;

public interface INatureStorage
{
    @Slow
    void setCityCreationDenied(@NotNull Nature nature, boolean denied) throws DataSourceException;

    @Slow
    void setName(@NotNull Nature nature, @NotNull String name) throws DataSourceException;
}
