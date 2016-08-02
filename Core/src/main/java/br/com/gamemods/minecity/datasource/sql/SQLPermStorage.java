package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.ExceptFlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.IExceptPermissionStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

//TODO Implementation
public class SQLPermStorage implements IExceptPermissionStorage
{
    @SuppressWarnings("UnusedParameters")
    public SQLPermStorage(SQLSource sqlSource, SQLConnection connection)
    {

    }

    @Override
    public void setDefaultMessage(@NotNull Message message)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deny(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag, @Nullable Message message)
            throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void allow(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void allowAll(@NotNull SimpleFlagHolder holder) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, boolean allow,
                    @NotNull Identity<?> identity, @Nullable Message message) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, @NotNull Identity<?> identity)
            throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public EnumMap<PermissionFlag, Message> loadSimplePermissions(@NotNull SimpleFlagHolder holder)
            throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }
}
