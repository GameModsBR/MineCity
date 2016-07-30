package br.com.gamemods.minecity.api.command;

public class CommandResult<R>
{
    public static final CommandResult SUCCESS = new CommandResult(null, true);
    public static final CommandResult ONLY_PLAYERS = new CommandResult(new Message("cmd.err.players-only",
            "Only players can execute this command."
    ));

    public final Message message;
    public final boolean success;
    public R result;

    public CommandResult(Message message)
    {
        this.message = message;
        this.success = false;
    }

    public CommandResult(Message message, boolean success)
    {
        this.message = message;
        this.success = success;
    }

    public CommandResult(Message message, R result)
    {
        this.message = message;
        this.success = true;
        this.result = result;
    }

    public CommandResult(Message message, R result, boolean success)
    {
        this.message = message;
        this.success = success;
        this.result = result;
    }

    @SuppressWarnings("unchecked")
    public static <T> CommandResult<T> success()
    {
        return SUCCESS;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        CommandResult<?> that = (CommandResult<?>) o;

        if(success != that.success) return false;
        if(message != null? !message.equals(that.message) : that.message != null) return false;
        return result != null? result.equals(that.result) : that.result == null;

    }

    @Override
    public int hashCode()
    {
        int result1 = message != null? message.hashCode() : 0;
        result1 = 31*result1 + (success? 1 : 0);
        result1 = 31*result1 + (result != null? result.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString()
    {
        return (success?"Success> ":"Error> ")+message;
    }
}
