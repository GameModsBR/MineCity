package br.com.gamemods.minecity.sponge.core.mixed;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public class Reactive<T>
{
    private final Supplier<Optional<T>> loader;
    private final ThreadLocal<Boolean> lock = new ThreadLocal<>();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<T> reactive;

    public Reactive(@NotNull Supplier<Optional<T>> loader)
    {
        this.loader = loader;
    }

    @NotNull
    public Optional<T> get()
    {
        if(reactive != null)
            return reactive;

        if(lock.get() == Boolean.TRUE)
            return Optional.empty();

        try
        {
            lock.set(true);
            return reactive = loader.get();
        }
        finally
        {
            lock.set(false);
        }
    }
}
