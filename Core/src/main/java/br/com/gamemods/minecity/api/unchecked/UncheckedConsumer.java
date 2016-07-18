package br.com.gamemods.minecity.api.unchecked;

import java.util.function.Consumer;

@FunctionalInterface
public interface UncheckedConsumer<T, E extends Exception> extends Consumer<T>, UncheckedWrapper
{
    @Override
    default void accept(T t)
    {
        try
        {
            checkedAccept(t);
        }
        catch(Exception e)
        {
            RuntimeException ex = wrapException(e);
            if(ex != null)
                throw ex;
        }
    }

    void checkedAccept(T t) throws E;
}
