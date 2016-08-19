package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Represents an object that may be assigned permissions
 */
public interface Permissible
{
    default void send(Message message){}
    default void send(Message[] messages){}

    @NotNull
    Identity<?> identity();

    @NotNull
    default Set<GroupID> getGroupIds()
    {
        return Collections.emptySet();
    }

    default boolean isGroupLoaded()
    {
        return true;
    }
}
