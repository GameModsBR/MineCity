package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface CommandFunction<R>
{
    default CommandResult<R> apply(CommandEvent event)
    {
        Throwable ex;
        try
        {
            return execute(event);
        }
        catch(InvocationTargetException e)
        {
            ex = e.getCause();
            if(ex == null)
                ex = e;
        }
        catch(Exception e)
        {
            ex = e;
        }

        if(ex instanceof UncheckedDataSourceException)
            ex = ex.getCause();

        ex.printStackTrace();
        return new CommandResult<>(new Message("cmd.exception",
                "Oops.. An error occurred while executing this command: ${error}",
                Message.errorArgs(ex)
        ));
    }

    default CommandResult<R> run(CommandEvent event)
    {
        CommandResult<R> result = apply(event);
        if(result.message != null)
        {
            Message message;
            if(result.success)
                 message = messageSuccess(result.message);
            else
                message = messageFailed(result.message);

            event.sender.send(message);
        }

        return result;
    }

    @NotNull
    static Message messageSuccess(@NotNull Message message)
    {
        return new Message("cmd.result.success",
                "<msg><blue><![CDATA[MineCity> ]]></blue><gray>${msg}</gray></msg>",
                new Object[]{"msg", message}
        );
    }

    @NotNull
    static Message messageFailed(@NotNull Message message)
    {
        return new Message("cmd.result.failed",
                "<msg><darkred><![CDATA[MineCity> ]]></darkred><red>${msg}</red></msg>",
                new Object[]{"msg", message}
        );
    }

    CommandResult<R> execute(CommandEvent cmd) throws Exception;
}
