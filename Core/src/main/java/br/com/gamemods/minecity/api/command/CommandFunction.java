package br.com.gamemods.minecity.api.command;

import java.util.List;

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
            e.printStackTrace();
            String message = e.getMessage();
            String simplified = e.getClass().getSimpleName();
            if(message != null)
                simplified += ": "+message;
            return new CommandResult<>(new Message("cmd.exception",
                    "Oops.. An error occurred while executing this command: ${error}",
                    new Object[]{"error",simplified}
            ));
        }
    }

    CommandResult<R> run(CommandSender sender, List<String> path, String[] args) throws Exception;
}
