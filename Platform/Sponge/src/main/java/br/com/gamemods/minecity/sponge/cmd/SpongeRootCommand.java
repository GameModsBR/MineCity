package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.command.CommandInfo;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpongeRootCommand implements CommandCallable
{
    private final MineCitySponge instance;
    private final String label;
    private final CommandInfo<?> info;

    public SpongeRootCommand(MineCitySponge instance, String label)
    {
        this.instance = instance;
        this.label = label;
        this.info = instance.mineCity.commands.get(label).get().command;
    }

    @NotNull
    @Override
    public CommandResult process(@NotNull CommandSource source, @NotNull String arguments) throws CommandException
    {
        String[] args = arguments.split("\\s+");
        List<String> path = new ArrayList<>(args.length + 1);
        path.add(label);
        path.addAll(Arrays.asList(args));
        instance.mineCity.commands.invoke(instance.sender(source, source), path);
        return CommandResult.success();
    }

    @NotNull
    @Override
    public List<String> getSuggestions(@NotNull CommandSource source, @NotNull String arguments, @Nullable Location<World> targetPosition) throws CommandException
    {
        String[] args = arguments.split("\\s+");
        String[] path = new String[args.length+1];
        path[0] = label;
        System.arraycopy(args, 0, path, 1, args.length);
        return instance.mineCity.commands.complete(path);
    }

    @Override
    public boolean testPermission(@NotNull CommandSource source)
    {
        return source.hasPermission(source.getActiveContexts(), info.permission+".exec");
    }

    @NotNull
    @Override
    public Optional<Text> getShortDescription(@NotNull CommandSource source)
    {
        String description = info.description;
        if(description == null || description.isEmpty())
            return Optional.empty();

        return Optional.of(Text.of(description));
    }

    @NotNull
    @Override
    public Optional<Text> getHelp(@NotNull CommandSource source)
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Text getUsage(@NotNull CommandSource source)
    {
        return Text.of("/"+label);
    }
}
