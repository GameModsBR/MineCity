package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.command.CommandInfo;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RootCommand<T> implements ICommand
{
    public final MineCityForge mod;
    public final String name;
    public final CommandInfo<T> info;

    public RootCommand(MineCityForge mod, CommandInfo<T> info)
    {
        this.mod = mod;
        this.info = info;
        name = info.getName();
    }

    @NotNull
    public String getCommandName()
    {
        return name;
    }

    @NotNull
    public String getCommandUsage(@NotNull ICommandSender sender)
    {
        return info.syntax;
    }

    @NotNull
    public List<String> getCommandAliases()
    {
        return new ArrayList<>(info.aliases);
    }

    public void processCommand(@NotNull ICommandSender sender, @NotNull String[] args)
    {
        List<String> path = new ArrayList<>(args.length + 1);
        path.add(name);
        path.addAll(Arrays.asList(args));
        mod.mineCity.commands.invoke(mod.sender(sender), path);
    }

    public void execute(@Nullable MinecraftServer server, @NotNull ICommandSender sender, @NotNull String[] args) throws CommandException
    {
        processCommand(sender, args);
    }

    public boolean canCommandSenderUseCommand(@NotNull ICommandSender sender)
    {
        return true;
    }

    public boolean checkPermission(@Nullable MinecraftServer server, @NotNull ICommandSender sender)
    {
        return true;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public List<String> addTabCompletionOptions(@NotNull ICommandSender sender, @NotNull String[] args)
    {
        String[] path = new String[args.length+1];
        path[0] = name;
        System.arraycopy(args, 0, path, 1, args.length);
        return mod.mineCity.commands.complete(path);
    }

    @NotNull
    public List<String> getTabCompletionOptions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String[] args,
                                                @Nullable BlockPos pos)
    {
        return addTabCompletionOptions(sender, args);
    }

    public boolean isUsernameIndex(@NotNull String[] p_82358_1_, int p_82358_2_)
    {
        return false;
    }

    public int compareTo(@NotNull ICommand o)
    {
        return this.getCommandName().compareTo(o.getCommandName());
    }
}
