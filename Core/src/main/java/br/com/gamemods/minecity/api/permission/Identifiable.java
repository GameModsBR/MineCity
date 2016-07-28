package br.com.gamemods.minecity.api.permission;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Identifiable<T>
{
    @NotNull
    T getUniqueId();

    @NotNull
    String getName();

    @NotNull
    default Identity<T> getIdentity()
    {
        return new Identity<>(getUniqueId(), getName());
    }
}
