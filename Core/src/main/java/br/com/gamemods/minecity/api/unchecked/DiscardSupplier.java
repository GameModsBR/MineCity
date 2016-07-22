package br.com.gamemods.minecity.api.unchecked;

@FunctionalInterface
public interface DiscardSupplier<T, E extends Exception> extends UncheckedSupplier<T, E>, DiscardWrapper
{
}
