package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class Message
{
    @NotNull
    private final String id;
    @NotNull
    private final String fallback;
    @Nullable
    private final Object[][] args;

    public static Object[][] errorArgs(Throwable ex)
    {
        String message = ex.getMessage();
        String simplified = ex.getClass().getSimpleName();
        if(message != null)
            simplified += ": "+message;

        return new Object[][]{{"error",simplified},
                {"className",ex.getClass().getSimpleName()},
                {"cause", Optional.ofNullable(ex.getMessage()).orElse("")}};
    }

    public static Message list(Message[] messages)
    {
        return list(messages, new Message("",", "));
    }

    public static Message list(Message[] messages, Message join)
    {
        Object[][] args = new Object[messages.length+1][2];
        args[args.length-1][0] = "join";
        args[args.length-1][1] = join;

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < messages.length; i++)
        {
            sb.append("${").append(i).append("}").append("${join}");
            args[i][0] = i;
            args[i][1] = messages[i];
        }

        if(sb.length() > 0)
            sb.setLength(sb.length()-7);

        return new Message("", sb.toString(), args);
    }

    public Message(@NotNull String id, @Nullable String fallback, @Nullable Object[]... args)
    {
        this.id = id;
        this.fallback = fallback == null? id : fallback;
        this.args = args == null? null : args.length == 0? null : args.clone();
    }

    public Message(@NotNull String id, @Nullable String simple)
    {
        this(id, simple, (Object[][]) null);
    }

    public Message(@NotNull String simple)
    {
        this("", simple);
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
        return args == null? null :args.clone();
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

        return id.isEmpty() == message.id.isEmpty()
                && !(id.isEmpty() && !message.fallback.equals(fallback))
                && id.equals(message.id)
                && Arrays.deepEquals(args, message.args);
    }

    @Override
    public int hashCode()
    {
        int result = id.isEmpty()? fallback.hashCode() : id.hashCode();
        result = 31*result + Arrays.deepHashCode(args);
        return result;
    }
}
