package br.com.gamemods.minecity.api.unchecked;

@FunctionalInterface
public interface DiscardConsumer<T, E extends Exception> extends UncheckedConsumer<T, E>, DiscardWrapper
{
}
