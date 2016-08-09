package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.datasource.api.IDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public final class CommandTree
{
    private Map<String, CommandEntry> tree = new HashMap<>();
    private Map<String, CommandDefinition> commands = new HashMap<>();
    public IDataSource dataSource;
    public Consumer<Runnable> scheduler = Runnable::run;
    public Supplier<Stream<String>> onlinePlayers = Stream::empty;
    public Supplier<Stream<String>> cityNames = Stream::empty;
    private CommandGroup root = new CommandGroup(new CommandInfo<>("", this::groupExecutor));

    public CommandTree()
    {
        root.subTree = tree;
    }

    public CommandResult invoke(CommandSender sender, String args)
    {
        return invoke(sender, toList(args));
    }

    public CommandResult invoke(CommandSender sender, List<String> args)
    {
        Optional<Result> resultOpt = get(args);
        if(!resultOpt.isPresent())
            return new CommandResult(new Message("cmd.not-found",
                    "Unknown command: /${base}",
                    new Object[]{"base",args.get(0)}
            ));

        Result result = resultOpt.get();
        if(result.command.async)
        {
            scheduler.accept( ()-> result.run(sender) );
            return CommandResult.success();
        }

        return result.run(sender);
    }

    public Set<String> getRootCommands()
    {
        return Collections.unmodifiableSet(tree.keySet());
    }

    public void registerCommand(String id, Arg[] args, boolean async, CommandFunction<?> function)
    {
        registerCommand(id, new CommandDefinition(args, async, function));
    }

    private void registerCommand(String id, CommandDefinition def)
    {
        commands.put(id, def);
        walk(tree, id, def);
    }

    private void walk(Map<String, CommandEntry> tree, String id, CommandDefinition def)
    {
        if(tree == null)
            return;

        for(CommandEntry entry: tree.values())
        {
            CommandInfo info = entry.getInfo();
            if(id.equals(info.commandId))
            {
                info.function = def.function;
                info.args = def.args;
                info.async = def.async;
            }

            walk(entry.getSubTree(), id, def);
        }
    }

    public void registerCommand(String id, boolean console, boolean async, Arg[] argSet, @Nullable Object instance, @NotNull Method method)
    {
        registerCommand(id, new CommandDefinition(argSet, async, (cmd) -> {
            if(!console && !cmd.sender.isPlayer())
                return CommandResult.ONLY_PLAYERS;

            Object result = method.invoke(instance, cmd);
            Class<?> returnType = method.getReturnType();
            if(result == null)
            {
                if(returnType.equals(Void.TYPE) || returnType.equals(Message.class))
                    return CommandResult.SUCCESS;

                return new CommandResult(null, false);
            }

            if(result instanceof CommandResult)
                return (CommandResult) result;
            if(result instanceof Message)
                return new CommandResult((Message)result);

            if(Boolean.FALSE.equals(result))
                return new CommandResult(null, false);

            return CommandResult.SUCCESS;
        }));
    }

    public void registerCommands(@NotNull Object commands)
    {
        Object instance = commands;
        Class c;
        if(commands instanceof Class)
        {
            c = (Class) commands;
            instance = null;
        }
        else
            c = commands.getClass();

        Class<?>[] expected = {CommandEvent.class};
        for(Method method: c.getMethods())
        {
            int modifiers = method.getModifiers();
            if(!Modifier.isPublic(modifiers) || instance == null && !Modifier.isStatic(modifiers)
                    || !method.isAnnotationPresent(Command.class))
                continue;

            for(Annotation annotation: method.getAnnotations())
            {
                if(!(annotation instanceof Command))
                    continue;

                if(!Arrays.equals(expected, method.getParameterTypes()))
                {
                    System.err.println("The method "+method+" has @Command annotation but has an invalid signature!");
                    continue;
                }

                Command command = (Command) annotation;
                boolean async = method.isAnnotationPresent(Async.class);
                registerCommand(command.value(), command.console(), async, command.args(), instance, method);
            }
        }
    }

    public void parseXml(InputStream xml)  throws IOException, SAXException
    {
        Document doc;
        try
        {
            doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(xml);
        }
        catch(ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }

        Element root = doc.getDocumentElement();
        if(!root.getTagName().equals("minecity-commands"))
            throw new IllegalArgumentException("This is not a minecity-commands XML file! Root: "+root.getTagName());

        try
        {
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression descPath = xPath.compile("desc");
            XPathExpression permPath = xPath.compile("permission");
            XPathExpression syntaxPath = xPath.compile("syntax");

            Map<String, CommandInfo> groups = new HashMap<>();
            Map<String, Set<String>> groupTree = new HashMap<>();
            Map<String, List<CommandInfo>> commands = new HashMap<>();

            NodeList nodes = (NodeList) xPath.evaluate("groups/group | commands/command", root, XPathConstants.NODESET);
            int len = nodes.getLength();
            for(int i = 0; i < len; i++)
            {
                Node node = nodes.item(i);
                CommandInfo info = new CommandInfo<>("");
                info.description = descPath.evaluate(node);
                info.syntax = syntaxPath.evaluate(node);
                info.permission = permPath.evaluate(node);

                NamedNodeMap attributes = node.getAttributes();
                String id = attributes.getNamedItem("id").getTextContent();

                if(info.permission.isEmpty())
                    info.permission = "minecity.cmd."+id;

                Set<String> parents = new HashSet<>(parseIds(attributes.getNamedItem("parent")));
                info.aliases = new LinkedHashSet<>(parseIds(attributes.getNamedItem("cmd")));
                if(info.getName().equals(""))
                    throw new InvalidPropertiesFormatException("missing cmd attribute for "+node.getNodeName()+" "+id);


                if(node.getNodeName().equals("group"))
                {
                    info.function = this::groupExecutor;
                    String identity = identity(id);
                    groups.put(identity, info);
                    groupTree.put(identity, new HashSet<>());
                    parents.forEach(parent -> {
                        Set<String> children = groupTree.get(parent);
                        if(children == null) groupTree.put(parent, children = new HashSet<>());
                        children.add(identity);
                    });
                }
                else
                {
                    CommandDefinition commandDefinition = this.commands.computeIfAbsent(id, (s)-> new CommandDefinition());
                    info.commandId = id;
                    info.function = commandDefinition.function;
                    info.args = commandDefinition.args;
                    info.async = commandDefinition.async;
                    for(String parent: parents)
                    {
                        List<CommandInfo> commandList = commands.get(parent);
                        if(commandList == null) commands.put(parent, commandList = new ArrayList<>());
                        commandList.add(info);
                    }
                }
            }

            walk(groups, groupTree, commands);
        }
        catch(XPathExpressionException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void walk(Map<String, CommandInfo> groups, Map<String, Set<String>> groupTree, Map<String, List<CommandInfo>> commands)
            throws InvalidPropertiesFormatException
    {
        walk(groups, groupTree, commands, "", Collections.singletonList(""));
    }

    private void walk(Map<String, CommandInfo> groups, Map<String, Set<String>> groupTree, Map<String, List<CommandInfo>> commands,
              String current, List<String> path) throws InvalidPropertiesFormatException
    {
        Set<String> subTree = groupTree.get(current);
        if(subTree == null)
            throw new InvalidPropertiesFormatException("No subtree information found: "+path);

        for(String childGroup: subTree)
        {
            CommandInfo<?> group = groups.get(childGroup);
            if(group == null)
                throw new InvalidPropertiesFormatException("Missing group definition: "+childGroup);
            register(path, group, true);

            List<String> subPath = new ArrayList<>(path);
            subPath.add(group.getName());
            for(CommandInfo subCommand: commands.getOrDefault(childGroup, Collections.emptyList()))
                register(subPath, subCommand, false);

            walk(groups, groupTree, commands, childGroup, subPath);
        }
    }

    private List<String> parseIds(Node node)
    {
        if(node == null || node.getTextContent() == null)
            return Collections.singletonList("");
        else
            return Arrays.stream(node.getTextContent().split(",")).map(StringUtil::identity)
                    .collect(Collectors.toList());
    }

    public CommandResult<Void> groupExecutor(CommandEvent cmd)
    {
        Map<String, CommandEntry> subTree= get(cmd.path).map(r->r.entry).map(CommandEntry::getSubTree).orElseGet(HashMap::new);

        return new CommandResult<>(new Message("todo.group.list", "Group List: ${child}",
                new Object[]{"child", subTree.keySet()}
        ), cmd.args.isEmpty());
    }

    public void register(@NotNull String path, @NotNull CommandInfo info, boolean group)
            throws IllegalArgumentException
    {
        register(toList(path), info, group);
    }

    private List<String> toList(String path)
    {
        return path.isEmpty()? Collections.emptyList() : Arrays.asList(path.trim().split("\\s+"));
    }

    public void register(@NotNull List<String> path, @NotNull CommandInfo<?> info, boolean group)
            throws IllegalArgumentException
    {
        Map<String, CommandEntry> subTree = tree;

        for(int i = 0; i < path.size(); i++)
        {
            String cmd = path.get(i);
            if(cmd.isEmpty()) continue;

            CommandEntry entry = subTree.get(identity(cmd));
            if(entry == null)
                throw new IllegalArgumentException("Path not found: "+path.subList(0, i+1));

            subTree = entry.getSubTree();
            if(subTree == null)
                throw new IllegalArgumentException("This is not a group: "+path.subList(0, i+1));
        }

        CommandEntry entry = group? new CommandGroup(info) : new CommandInfoEntry(info);
        for(String name: info.aliases)
        {
            if(subTree.containsKey(name))
                throw new IllegalStateException("Key already defined, key: "+name+", group: "+path);

            subTree.put(name, entry);
        }
    }

    public Optional<Result> get(String[] args)
    {
        return get(Arrays.asList(args));
    }

    public Optional<Result> get(String line)
    {
        return get(toList(line));
    }

    public Optional<Result> get(List<String> args)
    {
        if(args.isEmpty())
            args = Collections.singletonList("");

        Map<String, CommandEntry> subTree = tree;
        CommandEntry command = null;
        List<String> path = new ArrayList<>();
        int i;
        Iterator<String> iter = args.iterator();
        for(i = 0; subTree != null && iter.hasNext(); i++)
        {
            String arg = iter.next();
            CommandEntry entry = subTree.get(identity(arg));
            if(entry == null || entry.getInfo().function == null)
                break;

            command = entry;
            subTree = entry.getSubTree();
            path.add(arg);
        }

        if(command == null)
            return Optional.empty();

        Result result = new Result(command.getInfo(), args.subList(i, args.size()), path);
        result.entry = command;
        return Optional.of(result);
    }

    protected List<String> completeFunction(Arg[] defs, @NotNull List<String> args, @NotNull String search)
    {
        if(defs == null || defs.length == 0)
        {
            if(search.isEmpty())
                return Collections.emptyList();
            else
            {
                String lower = search.toLowerCase();
                return onlinePlayers.get().filter(p -> p.toLowerCase().startsWith(lower)).sorted()
                        .collect(Collectors.toList());
            }
        }

        Arg def;
        String arg;
        if(args.size() + 1 > defs.length)
        {
            def = defs[defs.length - 1];
            if(!def.sticky())
                return Collections.emptyList();

            arg = String.join(" ", args) + " " + search;
        }
        else if(args.isEmpty())
        {
            def = defs[0];
            arg = search;
        }
        else
        {
            def = defs[args.size()];
            arg = search;
        }

        Stream<String> options = null;
        String key = arg.toLowerCase();
        Predicate<String> filter = o -> o.toLowerCase().startsWith(key);
        Arg.Type type = def.type();
        switch(type)
        {
            case PLAYER:
                options = onlinePlayers.get();
                break;
            case PREDEFINED:
                options = Stream.of(def.options());
                break;
            case UNDEFINED:
                if(arg.isEmpty())
                    return Collections.emptyList();
                options = Stream.concat(onlinePlayers.get(), cityNames.get());
                break;
            case GROUP_OR_CITY:
            case GROUP:
            {
                String relativeName = def.relative();
                int index = -1;
                for(int i = 0; i < defs.length; i++)
                {
                    if(defs[i].name().equals(relativeName))
                    {
                        index = i;
                        break;
                    }
                }

                if(index >= 0 && args.size() >= index)
                {
                    Optional<Set<String>> groups = dataSource.getGroupNames(args.get(index));

                    if(groups.isPresent())
                    {
                        options = groups.get().stream();
                        String id = identity(key);
                        filter = o -> identity(o).startsWith(id);
                    }
                }

                if(type == Arg.Type.GROUP)
                    break;
            }
            case CITY:
            {
                options = cityNames.get();
                String id = identity(key);
                filter = o-> identity(o).startsWith(id);
                break;
            }
            default:
                return Collections.emptyList();
        }

        if(options == null)
            return Collections.emptyList();

        if(!arg.isEmpty())
            options = options.filter(filter);

        if(!args.isEmpty() && def.sticky())
        {
            List<String> identities = args.stream().map(StringUtil::identity).collect(Collectors.toList());
            options = options.map(o-> {
                Queue<String> parts = new ArrayDeque<>(Arrays.asList(o.split("\\s+")));
                Iterator<String> iter = identities.iterator();
                while(!(parts.isEmpty()) && iter.hasNext())
                {
                    String next = iter.next();
                    String identity = identity(parts.element());
                    if(identity.equals(next))
                        parts.remove();
                    else if(identity.startsWith(next))
                        return null;
                    else
                        break;
                }
                return String.join(" ", parts);
            }).filter(o-> o != null);
        }

        return options.sorted().flatMap(s-> Stream.of(s.replaceAll("\\s", ""), s.split("\\s",2)[0])).distinct().collect(Collectors.toList());
    }

    private Result rootResult(List<String> args)
    {
        return new Result(root.getInfo(), args, Collections.emptyList(), root);
    }

    public List<String> complete(String[] args)
    {
        return complete(Arrays.asList(args));
    }

    public List<String> complete(List<String> args)
    {
        List<String> path = args.subList(0, args.size() -1);
        Result result = get(path).orElseGet(()-> rootResult(path));
        String search = args.get(args.size()-1).toLowerCase();

        Map<String, CommandEntry> subTree = result.entry.getSubTree();
        if(subTree == null)
            return completeFunction(result.entry.getInfo().args, result.args, search);

        if(!result.args.isEmpty())
            return Collections.emptyList();

        Stream<String> stream;
        if(search.isEmpty())
            stream = subTree.values().stream().map(CommandEntry::getInfo).map(CommandInfo::getName).distinct();
        else
            stream = subTree.keySet().stream().filter(k-> k.toLowerCase().startsWith(search));

        return stream.sorted().collect(Collectors.toList());
    }

    static class CommandDefinition
    {
        CommandFunction<?> function;
        Arg[] args;
        boolean async;

        public CommandDefinition(Arg[] args, boolean async, CommandFunction function)
        {
            this.args = args;
            this.async = async;
            this.function = function;
        }

        public CommandDefinition()
        {}
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

    static class CommandInfoEntry implements CommandEntry
    {
        CommandInfo command;

        public CommandInfoEntry(CommandInfo command)
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
        public final CommandInfo<?> command;
        public final List<String> args;
        public final List<String> path;
        private CommandEntry entry;

        public Result(CommandInfo command, List<String> args, List<String> path)
        {
            this.command = command;
            this.args = args;
            this.path = path;
        }

        private Result(CommandInfo<?> command, List<String> args, List<String> path,
                      CommandEntry entry)
        {
            this.command = command;
            this.args = args;
            this.path = path;
            this.entry = entry;
        }

        public CommandResult run(CommandSender sender)
        {
            return command.function.run(new CommandEvent(sender, path, args));
        }

        @Override
        public String toString()
        {
            return "Result{" +
                    "command=" + command +
                    ", args=" + args +
                    ", path=" + path +
                    '}';
        }
    }
}
