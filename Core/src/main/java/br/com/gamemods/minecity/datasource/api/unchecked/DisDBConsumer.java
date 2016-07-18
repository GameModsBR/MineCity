package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.DiscardConsumer;
import br.com.gamemods.minecity.datasource.api.DataSourceException;

@FunctionalInterface
public interface DisDBConsumer<T> extends DiscardConsumer<T, DataSourceException>
{
}
