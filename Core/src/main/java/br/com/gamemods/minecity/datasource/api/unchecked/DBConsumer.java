package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.UncheckedConsumer;
import br.com.gamemods.minecity.datasource.api.DataSourceException;

public interface DBConsumer<T> extends UncheckedConsumer<T, DataSourceException>, UncheckedDBWrapper
{
}
