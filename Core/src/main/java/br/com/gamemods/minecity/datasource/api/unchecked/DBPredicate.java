package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.UncheckedPredicate;
import br.com.gamemods.minecity.datasource.api.DataSourceException;

@FunctionalInterface
public interface DBPredicate<T> extends UncheckedPredicate<T, DataSourceException>, UncheckedDBWrapper
{
}
