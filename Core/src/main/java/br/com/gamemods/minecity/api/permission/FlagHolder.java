package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface FlagHolder
{
    Message DEFAULT_DENIAL_MESSAGE = new Message("action.denied", "You don't have permission to perform this action.");

    /**
     * The actual owner of this holder
     * @return {@code null} if the holder is owned by the server administrators
     */
    @Nullable
    default PlayerID owner()
    {
        return null;
    }

    @NotNull
    Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action);

    @NotNull
    Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action);

    static Supplier<Optional<Message>> can(MinecraftEntity entity, PermissionFlag flag, FlagHolder holder)
    {
        if(holder == null)
            return Optional::empty;

        return ()-> holder.can(entity, flag);
    }

    static Supplier<Optional<Message>> can(Identity<?> identity, PermissionFlag flag, FlagHolder holder)
    {
        if(holder == null)
            return Optional::empty;

        return ()-> holder.can(identity, flag);
    }

    static Stream<Message> can(MinecraftEntity entity, PermissionFlag flag, FlagHolder... holders)
    {
        return Stream.of(holders)
                .map(h-> can(entity, flag, h))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                ;
    }

    static Stream<Message> can(Identity<?> identity, PermissionFlag flag, FlagHolder... holders)
    {
        return Stream.of(holders)
                .map(h-> can(identity, flag, h))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                ;
    }

    static Message wrapDeny(Message message)
    {
        return new Message("", LegacyFormat.RED+"${msg}", new Object[]{"msg", message});
    }
}
