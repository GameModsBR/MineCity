package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.ExceptFlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface IExceptPermissionStorage extends ISimplePermissionStorage
{
    @Slow
    void set(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, boolean allow,
             @NotNull Identity<?> identity, @Nullable Message message)
            throws DataSourceException;

    @Slow
    void remove(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, @NotNull Identity<?> identity)
            throws DataSourceException;

    @Slow
    @NotNull
    Map<PermissionFlag,Map<Identity<?>,Optional<Message>>> loadExceptPermissions(@NotNull ExceptFlagHolder holder)
            throws DataSourceException;
}
