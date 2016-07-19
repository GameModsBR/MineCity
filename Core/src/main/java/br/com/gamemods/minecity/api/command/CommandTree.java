package br.com.gamemods.minecity.api.command;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class CommandTree
{
    private Map<String, CommandEntry> tree = new HashMap<>();

    public CommandResult execute(CommandSender sender, String args)
    {
        return execute(sender, toArray(args));
    }

    public CommandResult execute(CommandSender sender, String[] args)
    {
        return get(args).map(r-> r.run(sender)).orElseGet(()->
            new CommandResult(new Message("cmd.not-found", "Unknown command: /${base}",
                    new Object[]{"base",args[0]}
            )
        ));
    }

    public void register(@NotNull String path, @NotNull String name, @NotNull CommandInfo info, boolean group)
            throws IllegalArgumentException
    {
        register(toArray(path), name, info, group);
    }

    private String[] toArray(String path)
    {
        return path.isEmpty()? new String[0] : path.trim().split("\\s+");
    }

    public void register(@NotNull String[] path, @NotNull String name, @NotNull CommandInfo info, boolean group)
            throws IllegalArgumentException
    {
        Map<String, CommandEntry> subTree = tree;

        for(int i = 0; i < path.length; i++)
        {
            String cmd = path[i];
            CommandEntry entry = subTree.get(identity(cmd));
            if(entry == null)
                throw new IllegalArgumentException("Path not found: "+Arrays.toString(Arrays.copyOf(path, i+1)));

            subTree = entry.getSubTree();
            if(subTree == null)
                throw new IllegalArgumentException("This is not a group: "+Arrays.toString(Arrays.copyOf(path, i+1)));
        }

        if(subTree.containsKey(name))
            throw new IllegalStateException("Key already defined, key: "+name+", group: "+Arrays.toString(path));

        subTree.put(name, group? new CommandGroup(info) : new Command(info));
    }

    public Optional<Result> get(String line)
    {
        return get(toArray(line));
    }

    public Optional<Result> get(String[] args)
    {
        Map<String, CommandEntry> subTree = tree;
        CommandInfo command = null;
        List<String> path = new ArrayList<>();
        int i;
        for(i = 0; subTree != null && i < args.length; i++)
        {
            String arg = args[i];
            CommandEntry entry = subTree.get(identity(arg));
            if(entry == null)
                break;

            command = entry.getInfo();
            subTree = entry.getSubTree();
            path.add(arg);
        }

        if(command == null)
            return Optional.empty();

        return Optional.of(new Result(command, Arrays.copyOfRange(args, i, args.length), path));
    }

    interface CommandEntry
    {
        Map<String, CommandEntry> getSubTree();
        CommandInfo getInfo();
    }

    static class CommandGroup implements CommandEntry
    {
        Map<String, CommandEntry> subTree = new HashMap<>();
        CommandInfo command;

        public CommandGroup(CommandInfo command)
        {
            this.command = command;
        }

        @Override
        public CommandInfo getInfo()
        {
            return command;
        }

        @Override
        public Map<String, CommandEntry> getSubTree()
        {
            return subTree;
        }
    }

    static class Command implements CommandEntry
    {
        CommandInfo command;

        public Command(CommandInfo command)
        {
            this.command = command;
        }

        @Override
        public CommandInfo getInfo()
        {
            return command;
        }

        @Override
        public Map<String, CommandEntry> getSubTree()
        {
            return null;
        }
    }

    public static class Result
    {
        public final CommandInfo command;
        public final String[] args;
        public final List<String> path;

        public Result(CommandInfo command, String[] args, List<String> path)
        {
            this.command = command;
            this.args = args;
            this.path = path;
        }

        public CommandResult run(CommandSender sender)
        {
            return command.function.apply(sender, path, args);
        }

        @Override
        public String toString()
        {
            return "Result{" +
                    "command=" + command +
                    ", args=" + Arrays.toString(args) +
                    ", path=" + path +
                    '}';
        }
    }
}
