package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface FlagHolder
{
    @NotNull
    Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action);

    static Supplier<Optional<Message>> can(MinecraftEntity entity, PermissionFlag flag, FlagHolder holder)
    {
        if(holder == null)
            return Optional::empty;

        return ()-> holder.can(entity, flag);
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
}
