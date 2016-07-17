package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.UncheckedFunction;
import br.com.gamemods.minecity.datasource.api.DataSourceException;

public interface DBFunction<T,R> extends UncheckedFunction<T,R,DataSourceException>, UncheckedDBWrapper
{
}
