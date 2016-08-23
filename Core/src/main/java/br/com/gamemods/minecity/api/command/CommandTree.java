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
import java.util.function.BiFunction;
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
    public BiFunction<String, Arg[], Arg[]> optionTransformer = (id, args)-> args;
    public Consumer<Runnable> scheduler = Runnable::run;
    public Supplier<Stream<String>> onlinePlayers = Stream::empty;
    public Supplier<Stream<String>> cityNames = Stream::empty;
    public Supplier<MessageTransformer> messageTransformer;
    private CommandGroup root = new CommandGroup(new CommandInfo<>("", this::groupExecutor));

    public CommandTree()
    {
        root.subTree = tree;
        registerCommands(this);
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

        if(result.command.commandId != null && !sender.hasPermission("minecity.cmd."+result.command.commandId))
        {
            CommandResult fail = CommandResult.noPermission();
            sender.send(CommandFunction.messageFailed(fail.message));
            return fail;
        }

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
                info.translatedArg = info.args != null && Arrays.stream(info.args).anyMatch(arg -> arg instanceof TranslatedOptions);
                info.async = def.async;
            }

            walk(entry.getSubTree(), id, def);
        }
    }

    public void registerCommand(String id, boolean console, boolean async, Arg[] argSet, @Nullable Object instance, @NotNull Method method)
    {
        if(Arrays.stream(argSet).map(Arg::options).filter(o-> o.length > 0).findAny().isPresent())
            argSet = optionTransformer.apply(id, argSet);

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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            doc = factory.newDocumentBuilder().parse(xml);
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
                    info.translatedArg = info.args != null && Arrays.stream(info.args).anyMatch(arg -> arg instanceof TranslatedOptions);
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

    private static Message formatArg(String id, Arg arg)
    {
        Message val = new Message("cmd."+id+".arg."+arg.name().toLowerCase().replaceAll("[^a-z0-9]+","-"), arg.name());
        if(arg.sticky())
            val = new Message("cmd.help.group.expanded.arg.sticky", "${arg}...", new Object[]{"arg", val});
        if(arg.optional())
            return new Message("cmd.help.group.expanded.arg.optional", "<msg><i>[${arg}]</i></msg>", new Object[]{"arg", val});
        else
            return new Message("cmd.help.group.expanded.arg.required", "${arg}", new Object[]{"arg", val});
    }

    private static Message fullArgs(CommandInfo info)
    {
        return info.args == null || info.args.length == 0?
                new Message("cmd.help.group.expanded.no-args", "")
                : info.args.length == 1?
                new Message("cmd.help.group.expanded.one-arg", "${arg}",
                        new Object[]{"arg", formatArg(info.commandId, info.args[0])}
                )
                :
                new Message("cmd.help.group.expanded.n-args.base", "${args}", new Object[]{
                        "args",
                        Message.list(
                                Arrays.stream(info.args)
                                        .map(arg -> formatArg(info.commandId, arg))
                                        .toArray(Message[]::new),
                                new Message("cmd.help.group.expanded.n-args.join", "<msg><gray><i>, </i></gray></msg>")
                        )
                });
    }

    @Command(value = "help", args = {@Arg(name = "command", sticky = true, optional = true, type = Arg.Type.HELP), @Arg(name = "page", optional = true, type = Arg.Type.HELP)})
    public CommandResult<?> help(CommandEvent cmd)
    {
        int page = 1;
        List<String> args = cmd.args instanceof ArrayList? cmd.args : new ArrayList<>(cmd.args);
        if(!args.isEmpty())
        {
            int index = args.size() - 1;
            String last = args.get(index);
            if(last.matches("^[0-9]+$"))
                page = Integer.parseInt(last);
            args.removeIf(str-> str.matches("^[0-9]+$"));
            args.addAll(0, cmd.path.subList(0, cmd.path.size()-1));
        }

        Result result = get(args).orElseGet(()-> get(cmd.path.subList(0, cmd.path.size()-1)).orElseGet(()-> rootResult(
                args)));
        String helpPath = (String.join(" ", cmd.path)+" "+String.join(" ", args.subList(Math.min(
                args.size(), cmd.path.size()-1), args.size()))).trim();
        Result shortest = result;
        for(int i = 1; i < result.path.size(); i++)
            shortest = get(result.path.subList(i, result.path.size())).filter(r-> r.entry.equals(result.entry)).orElse(shortest);

        if(result.entry instanceof CommandGroup)
        {
            CommandEntry[] items = result.entry.getSubTree().values().stream().distinct()
                    .sorted((a,b)->
                    {
                        boolean aRoot = get(a.getInfo().getName()).filter(r-> r.entry.equals(a)).map(r -> r.path.size()).orElse(2) == 1;
                        boolean bRoot = get(b.getInfo().getName()).filter(r-> r.entry.equals(b)).map(r -> r.path.size()).orElse(2) == 1;
                        if(aRoot == bRoot)
                            return a.getInfo().getName().compareToIgnoreCase(b.getInfo().getName());
                        else if(aRoot)
                            return 1;
                        else
                            return -1;
                    })
                    .toArray(CommandEntry[]::new);
            int itemsPerPage = 8;
            int pages = (int) Math.ceil( items.length / (double) itemsPerPage );
            page = Math.min(page, pages);
            int index = itemsPerPage * (page - 1);
            Message[] lines = new Message[2 + Math.min(itemsPerPage, items.length-index)];
            for(int i = 1; i < lines.length-1; index++, i++)
            {
                CommandEntry item = items[index];
                CommandInfo info = item.getInfo();
                Result shortestItem = shortest;
                List<String> path = new ArrayList<>(shortest.path.size());
                path.addAll(shortest.path);
                path.add(info.getName());
                for(int j = 0; j < path.size(); j++)
                    shortestItem = get(path.subList(j, path.size())).filter(r-> r.entry.equals(item)).orElse(shortestItem);
                boolean root = shortestItem.path.size() == 1;

                String fullCommand = "/"+String.join(" ", shortestItem.path);
                boolean repeat = true;
                int limit = 80;
                do
                {
                    String shortInfo = info.description == null? "":
                            info.description.length()>limit?
                                    info.description.substring(0,limit)+"..."
                                    :
                                    info.description;

                    Message message = new Message("cmd.help.group.item",
                            "<msg><hover>\n" +
                            "    <tooltip>\n" +
                            "        <aqua>${full-command}</aqua> <gold>${full-args}</gold>\n" +
                            "        <br/><br/>${full-info}\n" +
                            "    </tooltip>\n" +
                            "    <click>\n" +
                            "        <suggest cmd=\"${help-command}\"/>\n" +
                        "            <aqua>${command}${spacer}<gold>${short-args}</gold> <darkgray>-</darkgray> <gray>${short-info}</gray></aqua>\n" +
                            "    </click>\n" +
                            "</hover></msg>",
                            new Object[][]{
                                    {"spacer", info.args == null || info.args.length == 0? "" : " "},
                                    {"full-command", fullCommand},
                                    {"command", root? ("/"+ info.getName()) : info.getName()},
                                    {"help-command", "/"+helpPath+" "+ info.getName()},
                                    {"full-info", info.description == null? "" :
                                            Arrays.stream(info.description.trim().split("\\s+"))
                                            .reduce((a,b) -> a.matches(
                                                        "^(\n?[^\n ]+ [^\n ]+ [^\n ]+ [^\n ]+ [^\n ]+ [^\n ]+ [^\n ]+)+$"
                                                    )? a+"\n"+b
                                                     : a+" "+b
                                            ).get()
                                    },
                                    {"short-info", shortInfo},
                                    {"full-args", fullArgs(info)},
                                    {"short-args", info.args == null || info.args.length == 0?
                                                new Message("cmd.help.group.short.no-args", "")
                                            : info.args.length == 1?
                                                new Message("cmd.help.group.short.one-arg", "1 arg")
                                            :
                                                new Message("cmd.help.group.short.n-args", "${n} args",
                                                        new Object[]{"n", info.args.length}
                                                )
                                    }
                            }
                    );
                    if(limit < 80)
                        repeat = false;
                    else
                    {
                        int length = messageTransformer.get().toSimpleText(message).length();
                        if(length < 70)
                            repeat = false;
                        else
                            limit = Math.max(20, 60-(length-shortInfo.length()));
                    }
                    lines[i] = message;
                }while(repeat);
            }

            lines[0] =  new Message("cmd.help.group.header", "<msg><darkgreen>---<yellow>-=[MineCity Help]=-</yellow>----- <green>Click the items for info</green> -----</darkgreen></msg>");

            lines[lines.length-1] = pages == 1?
                    new Message("cmd.help.group.footer.one-page",
                            "<msg><green>\n" +
                            "    Page <gold>1</gold>/<gold>1</gold>\n" +
                            "    <darkgreen>---</darkgreen>\n" +
                            "    Tip: Click on the commands for more info\n" +
                            "</green></msg>")
                    : page == pages?
                    new Message("cmd.help.group.footer.last-page",
                            "<msg><green>\n" +
                            "    Page <gold>${page}</gold>/<gold>${total}</gold>\n" +
                            "    <darkgreen>---</darkgreen>\n" +
                            "    Tip: Click on the commands for more info\n" +
                            "</green></msg>",
                            new Object[][]{
                                    {"prev-page", "/"+ helpPath +" "+(page - 1)},
                                    {"page", page},
                                    {"total", pages}
                            })
                    :
                    new Message("cmd.help.group.footer.more-pages",
                            "<msg><green>\n" +
                            "    Page <gold>${page}</gold>/<gold>${total}</gold>\n" +
                            "    <darkgreen>---</darkgreen>\n" +
                            "    Next page: <gold>${next-page}</gold>\n" +
                            "</green></msg>",
                            new Object[][]{
                                    {"next-page", "/"+ helpPath +" "+(page + 1)},
                                    {"page", page},
                                    {"total", pages}
                            })
                    ;

            cmd.sender.send(lines);
            return CommandResult.success();
        }
        else
        {
            CommandInfo info = result.entry.getInfo();
            Message argsInfo;
            if(info.args == null || info.args.length == 0)
                argsInfo = new Message(
                        "cmd.help.info.args.no-args",
                        "<msg><gray>Arguments:<darkgray><![CDATA[\n  None]]></darkgray></gray></msg>"
                );
            else
                argsInfo = new Message(
                        "cmd.help.info.args.body",
                        "<msg><gray>Arguments:</gray><br/><darkgray><![CDATA[ * ${args}]]></darkgray></msg>",
                        new Object[]{
                            "args",
                            Message.list(
                                    Arrays.stream(info.args).map(arg ->
                                        new Message("cmd.help.info.args.details.body",
                                                "<msg><yellow>${short}</yellow><darkgray> : </darkgray>${type}. ${optional}. ${sticky}.</msg>",
                                                new Object[][]{
                                                        {"type",
                                                                arg.type() != Arg.Type.PREDEFINED || arg.options().length == 0?
                                                                    new Message(
                                                                        "cmd.help.info.args.details.type."+
                                                                            arg.type().name().toLowerCase().replace('_','-'),
                                                                        arg.type().name()
                                                                    )
                                                                : arg.options().length == 1?
                                                                    new Message(
                                                                        "cmd.help.info.args.details.type.single-predefinition",
                                                                        "${val}",
                                                                        new Object[]{"val", arg.options()[0]}
                                                                    )
                                                                :
                                                                    new Message(
                                                                        "cmd.help.info.args.details.type.multiple-predefinitions",
                                                                        "${val}",
                                                                        new Object[]{
                                                                            "val",
                                                                            Arrays.stream(arg.options())
                                                                                .distinct().sorted()
                                                                                .reduce((a,b)-> a+", "+b)
                                                                                .get()
                                                                        }
                                                                    )


                                                        },
                                                        {"short", formatArg(info.commandId, arg)},
                                                        {"optional", arg.optional()?
                                                                new Message(
                                                                        "cmd.help.info.args.details.props.optional",
                                                                        "Optional"
                                                                )
                                                                :
                                                                new Message(
                                                                        "cmd.help.info.args.details.props.required",
                                                                        "Required"
                                                                )
                                                        },
                                                        {"sticky", arg.sticky()?
                                                                new Message(
                                                                        "cmd.help.info.args.details.props.sticky",
                                                                        "Can use spaces"
                                                                )
                                                                :
                                                                new Message(
                                                                        "cmd.help.info.args.details.props.not-sticky",
                                                                        "Without spaces"
                                                                )
                                                        },
                                                })
                                    ).toArray(Message[]::new),
                                    new Message("cmd.help.info.args.join", "<msg><darkgray><![CDATA[\n * ]]></darkgray></msg>")
                            )
                        }
                );

            cmd.sender.send(new Message(
                    "cmd.help.info.body",
                    "<msg>" +
                        "<darkgreen>---<yellow>-=[MineCity Help]=-</yellow>--------------------</darkgreen><br/>\n" +
                        "<aqua>${full-command}</aqua> <yellow>${full-args}</yellow><br/>" +
                        "${args-info}<br/>" +
                        "${description}" +
                    "</msg>",
                    new Object[][]{
                            {"full-command","/"+String.join(" ", result.path)},
                            {"full-args", fullArgs(info)},
                            {"args-info", argsInfo},
                            {"more-commands", "/"+String.join(" ", cmd.path)+" "+String.join(" ",result.path.subList(cmd.path.size()-1, result.path.size()-1))},
                            {"description", info.description}
                    }
            ));
            return CommandResult.success();
        }
    }

    public CommandResult<Void> groupExecutor(CommandEvent cmd)
    {
        List<String> path = cmd.path instanceof ArrayList? cmd.path : new ArrayList<>(cmd.path);
        path.add("help");
        List<String> args = cmd.args instanceof ArrayList? cmd.args : new ArrayList<>(cmd.args);
        if(!args.isEmpty() && args.get(0).equals("help"))
            args.remove(0);
        help(path == cmd.path && args == cmd.args? cmd : new CommandEvent(cmd.sender, path, args));
        return CommandResult.failed();
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

    protected List<String> completeFunction(Arg[] defs, @NotNull List<String> args, @NotNull List<String> path, @NotNull String search)
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
            {
                if(defs.length == 1)
                    return Collections.emptyList();

                for(int i = defs.length - 2; i >= 0; i--)
                {
                    def = defs[i];
                    if(def.sticky())
                        break;
                }

                if(!def.sticky())
                    return Collections.emptyList();
            }

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

        boolean sort = true;
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
            case PLAYER_OR_CITY:
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
            case HELP:
            {
                List<String> helpPath = new ArrayList<>(path.size()+args.size()-1);
                helpPath.addAll(path.subList(0, path.size()-1));
                helpPath.addAll(args);
                helpPath.removeIf(str-> str.matches("^[0-9]+$"));
                Optional<Result> result = get(helpPath);
                Collection<CommandEntry> entries = result.filter(r-> r.args.isEmpty()).map(r -> r.entry.getSubTree())
                        .map(Map::values).orElse(Collections.emptyList());
                int limit = (int) Math.ceil(result.map(r-> r.entry.getSubTree())
                        .map(Map::values).map(Collection::stream).orElse(Stream.empty())
                        .distinct().count() / 8.0);

                if(limit <= 1 && entries.isEmpty())
                    return Collections.emptyList();

                options = Stream.concat(
                        entries.stream().map(CommandEntry::getInfo).map(CommandInfo::getName).distinct().sorted(),
                        Stream.generate(new Supplier<String>()
                        {
                            int current;
                            @Override
                            public String get()
                            {
                                return Integer.toString(++current);
                            }
                        }).limit(limit == 1? 0 : limit)
                );
                String starts = search.toLowerCase();
                filter = o -> o.toLowerCase().startsWith(starts);
                sort = false;
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

        if(sort)
            options = options.sorted();

        return options.flatMap(s-> Stream.of(s.replaceAll("\\s", ""), s.split("\\s",2)[0])).distinct().collect(Collectors.toList());
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
            return completeFunction(result.entry.getInfo().args, result.args, result.path, search);

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

        @Override
        public boolean equals(Object obj)
        {
            return obj == this || obj instanceof CommandGroup && subTree.equals(((CommandGroup) obj).subTree);
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

        @Override
        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;

            if(!(obj instanceof CommandInfoEntry))
                return false;

            CommandInfoEntry other = ((CommandInfoEntry) obj);
            return !(command == null || other.command == null) && command.commandId.equals(other.command.commandId);

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
            if(command.translatedArg && !args.isEmpty())
                reverseArgTranslation();
        }

        private Result(CommandInfo<?> command, List<String> args, List<String> path,
                      CommandEntry entry)
        {
            this.command = command;
            this.args = args;
            this.path = path;
            this.entry = entry;
            if(command.translatedArg && !args.isEmpty())
                reverseArgTranslation();
        }

        private void reverseArgTranslation()
        {
            int size = args.size();
            for(int i = 0; i < command.args.length && i < size; i++)
            {
                Arg arg = command.args[i];
                if(arg instanceof TranslatedOptions)
                {
                    List<String> translations = Arrays.stream(arg.options())
                            .map(String::toLowerCase).collect(Collectors.toList());

                    int index = translations.indexOf(args.get(i).toLowerCase());
                    if(index >= 0)
                        args.set(i, ((TranslatedOptions) arg).originalOptions()[index]);
                }
            }
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
