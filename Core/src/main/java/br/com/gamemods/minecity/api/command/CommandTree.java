package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.StringUtil;
import org.jetbrains.annotations.NotNull;
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
import java.util.stream.Collectors;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class CommandTree
{
    private Map<String, CommandEntry> tree = new HashMap<>();
    private Map<String, CommandFunction> functions = new HashMap<>();

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

    public void registerCommand(String id,CommandFunction function)
    {
        functions.put(id, function);
        walk(tree, id, function);
    }

    private void walk(Map<String, CommandEntry> tree, String id, CommandFunction function)
    {
        if(tree == null)
            return;

        for(CommandEntry entry: tree.values())
        {
            if(id.equals(entry.getInfo().commandId))
                entry.getInfo().function = function;

            walk(entry.getSubTree(), id, function);
        }
    }

    public void registerCommand(String id, boolean console, Object instance, Method method)
    {
        registerCommand(id, (sender, path, args) -> {
            if(!console && !sender.isPlayer())
                return CommandResult.ONLY_PLAYERS;

            Object result = method.invoke(instance, sender, path, args);
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
        });
    }

    public void registerCommands(Object commands)
    {
        Class c;
        if(commands instanceof Class)
        {
            c = (Class) commands;
            commands = null;
        }
        else
            c = commands.getClass();

        for(Method method: c.getMethods())
        {
            int modifiers = method.getModifiers();
            if(!Modifier.isPublic(modifiers) || commands == null && !Modifier.isStatic(modifiers)
                    || !method.isAnnotationPresent(Command.class))
                continue;

            for(Annotation annotation: method.getAnnotations())
            {
                if(!(annotation instanceof Command))
                    continue;

                Command command = (Command) annotation;
                registerCommand(command.value(), command.console(), commands, method);
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
                    info.commandId = id;
                    info.function = functions.get(id);
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
            for(CommandInfo subCommand: commands.get(childGroup))
                register(subPath, subCommand, false);

            walk(groups, groupTree, commands, childGroup, subPath);
        }
    }

    private List<String> parseIds(Node node)
    {
        if(node == null || node.getTextContent() == null)
            return Collections.singletonList("");
        else
            return Arrays.asList(node.getTextContent().split(",")).stream().map(StringUtil::identity)
                    .collect(Collectors.toList());
    }

    public CommandResult<Void> groupExecutor(CommandSender sender, List<String> path, String[] args)
    {
        Map<String, CommandEntry> subTree= get(path).map(r->r.entry).map(CommandEntry::getSubTree).orElseGet(HashMap::new);

        return new CommandResult<>(new Message("todo.group.list", "Group List: ${child}",
                new Object[]{"child", subTree.keySet()}
        ), args.length == 0);
    }

    public void register(@NotNull String path, @NotNull CommandInfo info, boolean group)
            throws IllegalArgumentException
    {
        register(toArray(path), info, group);
    }

    private String[] toArray(String path)
    {
        return path.isEmpty()? new String[0] : path.trim().split("\\s+");
    }

    public void register(@NotNull List<String> path, @NotNull CommandInfo info, boolean group)
    {
        register(path.toArray(new String[path.size()]), info, group);
    }

    public void register(@NotNull String[] path, @NotNull CommandInfo<?> info, boolean group)
            throws IllegalArgumentException
    {
        Map<String, CommandEntry> subTree = tree;

        for(int i = 0; i < path.length; i++)
        {
            String cmd = path[i];
            if(cmd.isEmpty()) continue;

            CommandEntry entry = subTree.get(identity(cmd));
            if(entry == null)
                throw new IllegalArgumentException("Path not found: "+Arrays.toString(Arrays.copyOf(path, i+1)));

            subTree = entry.getSubTree();
            if(subTree == null)
                throw new IllegalArgumentException("This is not a group: "+Arrays.toString(Arrays.copyOf(path, i+1)));
        }

        CommandEntry entry = group? new CommandGroup(info) : new CommandInfoEntry(info);
        for(String name: info.aliases)
        {
            if(subTree.containsKey(name))
                throw new IllegalStateException("Key already defined, key: "+name+", group: "+Arrays.toString(path));

            subTree.put(name, entry);
        }
    }

    public Optional<Result> get(List<String> args)
    {
        return get(args.toArray(new String[args.size()]));
    }

    public Optional<Result> get(String line)
    {
        return get(toArray(line));
    }

    public Optional<Result> get(String[] args)
    {
        Map<String, CommandEntry> subTree = tree;
        CommandEntry command = null;
        List<String> path = new ArrayList<>();
        int i;
        for(i = 0; subTree != null && i < args.length; i++)
        {
            String arg = args[i];
            if(arg.charAt(0) == '/')
                arg = arg.substring(1);
            CommandEntry entry = subTree.get(identity(arg));
            if(entry == null || entry.getInfo().function == null)
                break;

            command = entry;
            subTree = entry.getSubTree();
            path.add(arg);
        }

        if(command == null)
            return Optional.empty();

        Result result = new Result(command.getInfo(), Arrays.copyOfRange(args, i, args.length), path);
        result.entry = command;
        return Optional.of(result);
    }

    public Set<String> getRootCommands()
    {
        return Collections.unmodifiableSet(tree.keySet());
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
        public final String[] args;
        public final List<String> path;
        private CommandEntry entry;

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
