package br.com.gamemods.minecity.api.command;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface CommandFunction<R>
{
    default CommandResult<R> apply(CommandEvent event)
    {
        try
        {
            return execute(event);
        }
        catch(Exception e)
        {
            Throwable ex = e;
            if(e instanceof InvocationTargetException && e.getCause() != null)
                ex = e.getCause();

            ex.printStackTrace();
            String message = ex.getMessage();
            String simplified = ex.getClass().getSimpleName();
            if(message != null)
                simplified += ": "+message;
            return new CommandResult<>(new Message("cmd.exception",
                    "Oops.. An error occurred while executing this command: ${error}",
                    new Object[][]{{"error",simplified},
                            {"className",ex.getClass().getSimpleName()},
                            {"cause", Optional.ofNullable(ex.getMessage()).orElse("")}}
            ));
        }
    }

    default CommandResult<R> run(CommandEvent event)
    {
        CommandResult<R> result = apply(event);
        if(result.message != null)
        {
            Message message;
            if(result.success)
                 message = new Message("cmd.result.success",
                        "<msg><blue><![CDATA[MineCity> ]]></blue><gray>${msg}</gray></msg>",
                        new Object[]{"msg", result.message}
                );
            else
                message = new Message("cmd.result.failed",
                        "<msg><darkred><![CDATA[MineCity> ]]></darkred><red>${msg}</red></msg>",
                        new Object[]{"msg", result.message}
                );

            event.sender.send(message);
        }

        return result;
    }

    CommandResult<R> execute(CommandEvent cmd) throws Exception;
}
