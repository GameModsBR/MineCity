package br.com.gamemods.minecity.api.unchecked;

import java.util.function.Function;

@FunctionalInterface
public interface UncheckedFunction<T, R, E extends Exception> extends Function<T, R>, UncheckedWrapper
{
    @Override
    default R apply(T t)
    {
        try
        {
            return checkedApply(t);
        }
        catch(Exception e)
        {
            throw wrapException(e);
        }
    }

    R checkedApply(T t) throws E;
}
