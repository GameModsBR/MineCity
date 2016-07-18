package br.com.gamemods.minecity.api.unchecked;

public interface DiscardWrapper extends UncheckedWrapper
{
    @Override
    default RuntimeException wrapException(Exception e)
    {
        e.printStackTrace();
        return null;
    }
}
