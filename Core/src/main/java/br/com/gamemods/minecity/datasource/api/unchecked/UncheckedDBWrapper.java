package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.UncheckedWrapper;
import br.com.gamemods.minecity.datasource.api.DataSourceException;

public interface UncheckedDBWrapper extends UncheckedWrapper
{
    @Override
    default RuntimeException wrapException(Exception e)
    {
        if(e instanceof DataSourceException)
            return new UncheckedDataSourceException((DataSourceException) e);
        else if(e instanceof RuntimeException)
            return (RuntimeException) e;
        else
            return new RuntimeException(e);
    }
}
