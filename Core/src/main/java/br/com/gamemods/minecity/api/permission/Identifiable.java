package br.com.gamemods.minecity.api.permission;

import org.jetbrains.annotations.NotNull;

public interface Identifiable<T>
{
    @NotNull
    T getUniqueId();

    @NotNull
    String getName();

    @NotNull
    Identity<T> getIdentity();
}
