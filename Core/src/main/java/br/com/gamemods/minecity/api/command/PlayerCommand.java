package br.com.gamemods.minecity.api.command;

@FunctionalInterface
public interface PlayerCommand<R> extends CommandFunction<R>
{
    @Override
    @SuppressWarnings("unchecked")
    default CommandResult<R> execute(CommandEvent cmd) throws Exception
    {
        if(!cmd.sender.isPlayer())
            return CommandResult.ONLY_PLAYERS;

        return executePlayer(cmd);
    }

    CommandResult<R> executePlayer(CommandEvent cmd) throws Exception;
}
