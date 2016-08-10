package br.com.gamemods.minecity.api.unchecked;

@FunctionalInterface
public interface UncheckedRunnable<E extends Exception> extends Runnable,UncheckedWrapper
{
    @Override
    default void run()
    {
        try
        {
            runChecked();
        }
        catch(RuntimeException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw wrapException(e);
        }
    }

    void runChecked() throws E;
}
