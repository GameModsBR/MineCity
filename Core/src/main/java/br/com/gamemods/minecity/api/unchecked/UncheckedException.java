package br.com.gamemods.minecity.api.unchecked;

import org.jetbrains.annotations.NotNull;

public class UncheckedException extends RuntimeException
{
    private static final long serialVersionUID = -8259349155962616431L;

    public UncheckedException(@NotNull Throwable cause)
    {
        super(cause);
    }

    @NotNull
    @Override
    public synchronized Throwable getCause()
    {
        return super.getCause();
    }
}
