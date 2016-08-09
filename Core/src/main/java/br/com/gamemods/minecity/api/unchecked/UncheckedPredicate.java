package br.com.gamemods.minecity.api.unchecked;

import java.util.function.Predicate;

@FunctionalInterface
public interface UncheckedPredicate<T, E extends Exception> extends Predicate, UncheckedWrapper
{
    @Override
    default boolean test(Object o)
    {
        try
        {
            return testChecked(o);
        }
        catch(Exception e)
        {
            throw wrapException(e);
        }
    }

    boolean testChecked(Object o) throws E;
}
