package br.com.gamemods.minecity.api.unchecked;

import java.util.function.Supplier;

@FunctionalInterface
public interface UncheckedSupplier<T, E extends Exception> extends Supplier<T>, UncheckedWrapper
{
    @Override
    default T get() throws RuntimeException
    {
        try
        {
            return checkedGet();
        }
        catch(Exception e)
        {
            throw wrapException(e);
        }
    }

    T checkedGet() throws E;
}
