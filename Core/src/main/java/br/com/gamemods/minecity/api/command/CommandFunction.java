package br.com.gamemods.minecity.api.command;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface CommandFunction<R>
{
    default CommandResult<R> apply(CommandSender sender, List<String> path, String[] args)
    {
        try
        {
            return run(sender, path, args);
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

    CommandResult<R> run(CommandSender sender, List<String> path, String[] args) throws Exception;
}
