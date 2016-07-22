package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.UncheckedSupplier;
import br.com.gamemods.minecity.datasource.api.DataSourceException;

import java.sql.SQLException;

@FunctionalInterface
public interface DBSupplier<T> extends UncheckedSupplier<T, DataSourceException>, UncheckedDBWrapper
{
    @Override
    default T checkedGet() throws DataSourceException
    {
        try
        {
            return checkedDbGet();
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    T checkedDbGet() throws DataSourceException, SQLException;
}
