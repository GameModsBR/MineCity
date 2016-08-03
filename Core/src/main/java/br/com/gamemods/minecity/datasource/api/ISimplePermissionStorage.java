package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public interface ISimplePermissionStorage
{
    @Slow
    void setDefaultMessage(@NotNull SimpleFlagHolder holder, @Nullable Message message)
            throws DataSourceException;

    @Slow
    void deny(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag, @Nullable Message message)
            throws DataSourceException;

    @Slow
    void allow(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag)
        throws DataSourceException;

    @Slow
    void allowAll(@NotNull SimpleFlagHolder holder) throws DataSourceException;

    @NotNull
    EnumMap<PermissionFlag,Message> loadSimplePermissions(@NotNull SimpleFlagHolder holder) throws DataSourceException;

    void denyAll(SimpleFlagHolder holder, Map<? extends PermissionFlag, ? extends Message> flags) throws DataSourceException;
}
