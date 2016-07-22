package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.DiscardSupplier;
import br.com.gamemods.minecity.datasource.api.DataSourceException;

@FunctionalInterface
public interface DisDBSupplier <T> extends DiscardSupplier<T, DataSourceException>
{
}
