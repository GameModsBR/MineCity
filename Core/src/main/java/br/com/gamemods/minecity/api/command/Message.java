package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Message
{
    @NotNull
    private final String id;
    @NotNull
    private final String fallback;
    @Nullable
    private final Object[][] args;

    public Message(@NotNull String id, @Nullable String fallback, @Nullable Object[]... args)
    {
        this.id = id;
        this.fallback = fallback == null? id : fallback;
        this.args = args == null? null : args.length == 0? null : args;
    }

    public Message(@NotNull String id, @Nullable String simple)
    {
        this(id, simple, (Object[][]) null);
    }

    public Message(@NotNull String id)
    {
        this(id, id);
    }

    @NotNull
    public String getId()
    {
        return id;
    }

    @NotNull
    public String getFallback()
    {
        return fallback;
    }

    @Nullable
    public Object[][] getArgs()
    {
        return args;
    }

    @Override
    public String toString()
    {
        return StringUtil.replaceTokens(fallback, args);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if(!id.equals(message.id)) return false;
        return Arrays.deepEquals(args, message.args);

    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31*result + Arrays.deepHashCode(args);
        return result;
    }
}
