package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.UncheckedSupplier;

@FunctionalInterface
public interface DBSupplier<T> extends UncheckedSupplier<T, UncheckedDataSourceException>, UncheckedDBWrapper
{
}
