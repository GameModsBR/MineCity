package br.com.gamemods.minecity.api.permission;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface Identifiable<T extends Serializable>
{
    @NotNull
    T getUniqueId();

    @NotNull
    String getName();

    @NotNull
    Identity<T> getIdentity();
}
