package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.command.*;

public class GeneralCommands
{
    @Async
    @Command(value = "confirm", args = @Arg(name = "confirmation code", type = Arg.Type.PREDEFINED))
    public static CommandResult<?> confirm(CommandEvent cmd) throws Exception
    {
        if(!cmd.sender.isConfirmPending())
            return new CommandResult<>(new Message("cmd.confirm.not-pending", "You don't have any pending confirmation"));

        if(cmd.args.size() != 1)
            return new CommandResult<>(new Message("cmd.confirm.args", "Type the confirmation code to confirm the action or just ignore it if you don't want to continue."));

        CommandResult<CommandResult<?>> confirm = cmd.sender.confirm(cmd.args.get(0));
        if(!confirm.success)
            return new CommandResult<>(new Message("cmd.confirm.invalid", "You've typed an incorrect code, if you want to abort the operation just don't type the /confirm command."));

        return confirm.result;
    }
}
