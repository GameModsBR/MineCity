package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.command.CommandInfo;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RootCommand<T> implements ICommand
{
    public final MineCityForgeMod mod;
    public final String name;
    public final CommandInfo<T> info;

    public RootCommand(MineCityForgeMod mod, CommandInfo<T> info)
    {
        this.mod = mod;
        this.info = info;
        name = info.getName();
    }

    @Override
    public String getCommandName()
    {
        return name;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return info.syntax;
    }

    @Override
    public List getCommandAliases()
    {
        return new ArrayList<>(info.aliases);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        List<String> path = new ArrayList<>(args.length + 1);
        path.add(name);
        path.addAll(Arrays.asList(args));
        mod.mineCity.commands.invoke(mod.sender(sender), path);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        String[] path = new String[args.length+1];
        path[0] = name;
        System.arraycopy(args, 0, path, 1, args.length);
        return mod.mineCity.commands.complete(path);
    }

    @Override
    public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_)
    {
        return false;
    }

    @Override
    public int compareTo(@NotNull Object o)
    {
        return this.getCommandName().compareTo(((ICommand)o).getCommandName());
    }
}
