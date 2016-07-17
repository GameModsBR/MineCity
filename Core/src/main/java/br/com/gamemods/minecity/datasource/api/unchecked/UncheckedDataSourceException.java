package br.com.gamemods.minecity.datasource.api.unchecked;

import br.com.gamemods.minecity.api.unchecked.UncheckedException;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import org.jetbrains.annotations.NotNull;

public class UncheckedDataSourceException extends UncheckedException
{
    private static final long serialVersionUID = -17796973229021906L;

    public UncheckedDataSourceException(@NotNull DataSourceException cause)
    {
        super(cause);
    }

    @NotNull
    @Override
    public synchronized DataSourceException getCause()
    {
        return (DataSourceException) super.getCause();
    }
}
