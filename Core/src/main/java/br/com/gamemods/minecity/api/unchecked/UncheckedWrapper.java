package br.com.gamemods.minecity.api.unchecked;

public interface UncheckedWrapper
{
    default RuntimeException wrapException(Exception e)
    {
        return new RuntimeException(e);
    }
}
