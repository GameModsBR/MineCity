package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;

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
