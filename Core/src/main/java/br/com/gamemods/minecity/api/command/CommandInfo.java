package br.com.gamemods.minecity.api.command;

public class CommandInfo<R>
{
    public CommandFunction<R> function;

    public CommandInfo(CommandFunction<R> function)
    {
        this.function = function;
    }
}
