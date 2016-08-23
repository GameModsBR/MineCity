package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;

import java.util.*;

public class PermissionCommands
{
    private static final EnumSet<PermissionFlag> CITY_FLAGS = EnumSet.of(
            PermissionFlag.ENTER, PermissionFlag.CLICK, PermissionFlag.PICKUP, PermissionFlag.HARVEST,
            PermissionFlag.OPEN, PermissionFlag.PVP, PermissionFlag.PVC, PermissionFlag.MODIFY,
            PermissionFlag.VEHICLE, PermissionFlag.RIDE
    );
    private static final EnumSet<PermissionFlag> PLOT_FLAGS = EnumSet.copyOf(CITY_FLAGS);
    private static final EnumSet<PermissionFlag> NATURE_FLAGS = EnumSet.copyOf(CITY_FLAGS);

    private final MineCity mineCity;

    public PermissionCommands(MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    public void register(CommandTree tree)
    {
        tree.registerCommands(this);

        try
        {
            Arg[] list = PermissionCommands.class.getDeclaredMethod("list", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            Arg[] deny = PermissionCommands.class.getDeclaredMethod("deny", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            Arg[] allow = PermissionCommands.class.getDeclaredMethod("allow", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            Arg[] reset = PermissionCommands.class.getDeclaredMethod("reset", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            Arg[] denyAll = PermissionCommands.class.getDeclaredMethod("denyAll", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            Arg[] allowAll = PermissionCommands.class.getDeclaredMethod("allowAll", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            Arg[] resetAll = PermissionCommands.class.getDeclaredMethod("resetAll", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();

            for(PermissionFlag flag: CITY_FLAGS)
            {
                String name = flag.name().toLowerCase();
                tree.registerCommand("city.perms."+name, list, true, (CommandFunction) cmd-> list(cmd, flag));
                tree.registerCommand("city.deny."+name, deny, true, (PlayerCommand) cmd-> deny(cmd, flag));
                tree.registerCommand("city.allow."+name, allow, true, (PlayerCommand) cmd-> allow(cmd, flag));
                tree.registerCommand("city.clear."+name, reset, true, (PlayerCommand) cmd-> reset(cmd, flag));
                tree.registerCommand("city.deny.all."+name, denyAll, true, (PlayerCommand) cmd-> denyAll(cmd, flag));
                tree.registerCommand("city.allow.all."+name, allowAll, true, (PlayerCommand) cmd-> allowAll(cmd, flag));
                tree.registerCommand("city.clear.all."+name, resetAll, true, (PlayerCommand) cmd-> resetAll(cmd, flag));
            }

            list = PermissionCommands.class.getDeclaredMethod("listPlot", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            deny = PermissionCommands.class.getDeclaredMethod("denyPlot", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            allow = PermissionCommands.class.getDeclaredMethod("allowPlot", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            reset = PermissionCommands.class.getDeclaredMethod("resetPlot", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            denyAll = PermissionCommands.class.getDeclaredMethod("denyAllPlot", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            allowAll = PermissionCommands.class.getDeclaredMethod("allowAllPlot", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            resetAll = PermissionCommands.class.getDeclaredMethod("resetAllPlot", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();

            for(PermissionFlag flag: PLOT_FLAGS)
            {
                String name = flag.name().toLowerCase();
                tree.registerCommand("plot.perms."+name, list, true, (CommandFunction) cmd-> listPlot(cmd, flag));
                tree.registerCommand("plot.deny."+name, deny, true, (PlayerCommand) cmd-> denyPlot(cmd, flag));
                tree.registerCommand("plot.allow."+name, allow, true, (PlayerCommand) cmd-> allowPlot(cmd, flag));
                tree.registerCommand("plot.clear."+name, reset, true, (PlayerCommand) cmd-> resetPlot(cmd, flag));
                tree.registerCommand("plot.deny.all."+name, denyAll, true, (PlayerCommand) cmd-> denyAllPlot(cmd, flag));
                tree.registerCommand("plot.allow.all."+name, allowAll, true, (PlayerCommand) cmd-> allowAllPlot(cmd, flag));
                tree.registerCommand("plot.clear.all."+name, resetAll, true, (PlayerCommand) cmd-> resetAllPlot(cmd, flag));
            }

            list = PermissionCommands.class.getDeclaredMethod("listNature", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            deny = PermissionCommands.class.getDeclaredMethod("denyNature", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            allow = PermissionCommands.class.getDeclaredMethod("allowNature", CommandEvent.class, PermissionFlag.class).getAnnotation(Command.class).args();
            for(PermissionFlag flag: NATURE_FLAGS)
            {
                String name = flag.name().toLowerCase();
                tree.registerCommand("nature.perms."+name, list, false, (PlayerCommand) cmd-> listNature(cmd, flag));
                tree.registerCommand("nature.deny."+name, deny, true, (PlayerCommand) cmd-> denyNature(cmd, flag));
                tree.registerCommand("nature.allow."+name, allow, true, (PlayerCommand) cmd-> allowNature(cmd, flag));
            }
        }
        catch(ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Message list(SimpleFlagHolder holder, PermissionFlag flag, String prefix, String name)
    {
        Optional<Message> def = holder.can(flag);
        Map<Identity<?>, Message> exceptions = holder instanceof ExceptFlagHolder?
                ((ExceptFlagHolder)holder).getDirectPermissions(flag) :
                Collections.emptyMap()
                ;

        String flagKey = flag.name().toLowerCase().replace('_', '-');
        if(exceptions.isEmpty() || exceptions.values().stream().noneMatch(
                m->
                   m == null && def.isPresent()
                || m != null && !def.isPresent()
                || m != null && def.isPresent() && !m.equals(def.get())
        ))
        {
            if(def.isPresent())
                return new Message(
                        prefix+".nobody."+ flagKey +".no-exception",
                        "<msg><red><b>Nobody</b></red> can do that on ${name}, that will be shown when that happens:<br/><red>${msg}</red></msg>",
                        new Object[][]{
                                {"name", name},
                                {"msg", def.get()}
                        }
                );
            else
                return new Message(
                        prefix+".everybody."+ flagKey +".no-exception",
                        "<msg><green><b>Everybody</b></green> can do that on ${name}</msg>",
                        new Object[]{"name", name}
                );
        }

        Message except = Message.list(
                exceptions.entrySet().stream().map(e->
                        new Message(
                                prefix+".exceptions."+e.getKey().getType().name().toLowerCase().replace('_','-') +
                                        (e.getValue() == null?".allow" :".deny")
                                ,
                                e.getValue() == null?
                                        "<msg><green><b>${name}</b> - Is allowed</green></msg>" :
                                        "<msg><red><b>${name}</b> - Is denied with: ${msg}</red></msg>",
                                new Object[][]{
                                        {"msg", e.getValue()},
                                        {"name",
                                                e.getKey().getType() != Identity.Type.GROUP?
                                                        e.getKey().getName() :
                                                        new Message(
                                                                prefix+".exceptions.group.name",
                                                                "${group} from ${city}",
                                                                new Object[][]{
                                                                        {"group", e.getKey().getName()},
                                                                        {"city", ((GroupID)e.getKey()).home}
                                                                }
                                                        )
                                        }}
                        )).toArray(Message[]::new)
                ,
                new Message("", "\n"));

        if(def.isPresent())
            return new Message(
                    prefix+".nobody."+flagKey+".except",
                    "<msg><red><b>Nobody</b></red> <yellow>(${count} exceptions)</yellow> can do that on ${name}, that will be shown when that happens:" +
                            "<br/><red>${msg}</red><br/><br/>" +
                            "<yellow>Exceptions:</yellow><br/>${exceptions}</msg>",
                    new Object[][]{
                            {"name", name},
                            {"msg", def.get()},
                            {"exceptions", except},
                            {"count", exceptions.size()}
                    }
            );
        else
            return new Message(
                    prefix+".everybody."+flagKey+".except",
                    "<msg><green><b>Everybody</b></green> <yellow>(${count} exceptions)</yellow> can do that on ${name}"+
                            "<br/><br/>" +
                            "<yellow>Exceptions:</yellow><br/>${exceptions}</msg>",
                    new Object[][]{
                            {"name", name},
                            {"exceptions", except},
                            {"count", exceptions.size()}
                    }
            );
    }

    private Message[] list(SimpleFlagHolder holder, Set<PermissionFlag> filter, String prefix, String name)
    {
        Message[] lines = new Message[2 + filter.size()];
        lines[0] = new Message(prefix+".header", "<msg><darkgreen>----[${name}'s Permissions]---------------------</darkgreen></msg>",
                new Object[]{"name", name}
        );

        int i = 1;
        for(PermissionFlag flag: filter)
        {
            Optional<Message> def = holder.can(flag);
            Map<Identity<?>, Message> exceptions = holder instanceof ExceptFlagHolder?
                    ((ExceptFlagHolder) holder).getDirectPermissions(flag) :
                    Collections.emptyMap()
                    ;

            String key = flag.name().toLowerCase().replace('_', '-');
            Message permName = new Message(prefix+".perm."+key+".name", key.toLowerCase().replace('_',' '));
            lines[i++] = new Message("",
                    "${perm}${exceptions} ${description}",
                    new Object[][]{
                            {
                                    "perm",
                                    def.isPresent()?
                                            new Message(prefix + ".perm.denied",
                                                    "<msg><hover><tooltip>That message will be shown when this happens:<br/>" +
                                                            "<red>${reason}</red></tooltip><red>${perm}</red></hover></msg>",
                                                    new Object[][]{
                                                            {"perm", permName},
                                                            {"reason", def.get()}
                                                    }
                                            )
                                            :
                                            new Message(prefix + ".allowed", "<msg><green>${perm}</green></msg>", new Object[]{
                                                    "perm", permName
                                            })
                            },
                            {"description", new Message(prefix+".perm."+key+".info", "")},
                            {"exceptions", exceptions.isEmpty()? "":
                                    new Message(
                                            prefix+".exceptions.short",
                                            "<msg><hover><tooltip>${exceptions}</tooltip><yellow>${count} exceptions</yellow></hover></msg>",
                                            new Object[][]{
                                                    {"count", exceptions.size()},
                                                    {"exceptions", Message.list(
                                                            exceptions.entrySet().stream().map(e->
                                                                    new Message(
                                                                            prefix+".exceptions."+
                                                                                    e.getKey().getType().name()
                                                                                        .toLowerCase().replace('_','-') +
                                                                                    (e.getValue() == null?
                                                                                            ".allow" :
                                                                                            ".deny"
                                                                                    )
                                                                            ,
                                                                            e.getValue() == null?
                                                                                    "<msg><green><b>${name}</b> - Is allowed</green></msg>" :
                                                                                    "<msg><red><b>${name}</b> - Is denied with: ${msg}</red></msg>",
                                                                            new Object[][]{
                                                                                {"msg", e.getValue()},
                                                                                {"name",
                                                                                e.getKey().getType() != Identity.Type.GROUP?
                                                                                        e.getKey().getName() :
                                                                                        new Message(
                                                                                                prefix+".exceptions.group.name",
                                                                                                "${group} from ${city}",
                                                                                                new Object[][]{
                                                                                                        {"group", e.getKey().getName()},
                                                                                                        {"city", ((GroupID)e.getKey()).home}
                                                                                                }
                                                                                        )
                                                                            }}
                                                                    )).toArray(Message[]::new)
                                                            ,
                                                            new Message("", "\n")
                                                    )}
                                            }
                                    )
                            }
                    }
            );
        }

        lines[lines.length - 1] = new Message(
                prefix+".footer",
                "<msg><darkgreen>--------------------------------------------------</darkgreen></msg>"
        );

        return lines;
    }

    @Command(value = "#model:nature.perms", console = false)
    private CommandResult<?> listNature(CommandEvent cmd, PermissionFlag flag)
    {
        Nature nature = cmd.mineCity.nature(cmd.position.world);
        return new CommandResult<>(list(nature, flag, "cmd.nature.perms", nature.world.name()));
    }

    @Slow
    @Async
    @Command(value = "#model:city.perms", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    private CommandResult<?> list(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
        City city;
        if(cmd.args.isEmpty())
        {
            city = cmd.getChunk().getCity().orElse(null);
            if(city == null)
                return new CommandResult<>(new Message("cmd.city.perms.not-claimed", "You are not inside a city"));
        }
        else
        {
            String name = String.join(" ", cmd.args);
            city = mineCity.dataSource.getCityByName(name).orElse(null);
            if(city == null)
                return new CommandResult<>(new Message(
                        "cmd.city.perms.not-found",
                        "No city were found with name ${name}",
                        new Object[]{"name", name}
                ));
        }

        return new CommandResult<>(list(city, flag, "cmd.city.perms", city.getName()), true);
    }

    @Slow
    @Async
    @Command(value = "#model:plot.perms", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    private CommandResult<?> listPlot(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
        Plot plot;
        if(cmd.args.isEmpty())
        {
            if(cmd.position == null)
                return new CommandResult<>(new Message(
                        "cmd.plot.perms.type-city",
                        "Type a city name"
                ));

            plot = cmd.getChunk().getPlotAt(cmd.position.getBlock()).orElse(null);
            if(plot == null)
                return new CommandResult<>(new Message(
                        "cmd.plot.perms.not-claimed",
                        "You are not inside a plot"
                ));
        }
        else
        {
            plot = cmd.getChunk().getCity().flatMap(c-> c.getPlot(String.join(" ", cmd.args))).orElse(null);
            if(plot == null && cmd.args.size() > 1)
            {
                plot = mineCity.dataSource.getCityByName(cmd.args.get(0))
                        .flatMap(c-> c.getPlot(String.join(" ", cmd.args.subList(1, cmd.args.size()))))
                        .orElse(null)
                ;
            }

            if(plot == null)
                return new CommandResult<>(new Message(
                        "cmd.plot.perms.not-found",
                        "No plot was found with ${search}",
                        new Object[]{"search", String.join(" ", cmd.args)}
                ));
        }

        return new CommandResult<>(list(plot, flag, "cmd.plot.perms", plot.getName()), true);
    }

    @Command(value = "nature.perms", console = false)
    public CommandResult<?> listNature(CommandEvent cmd)
    {
        Nature nature = cmd.mineCity.nature(cmd.position.world);
        cmd.sender.send(list(nature, NATURE_FLAGS, "cmd.nature.perms", nature.world.name()));
        return CommandResult.success();
    }

    @Slow
    @Async
    @Command(value = "city.perms", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> list(CommandEvent cmd) throws DataSourceException
    {
        City city;
        if(cmd.args.isEmpty())
        {
            city = cmd.getChunk().getCity().orElse(null);
            if(city == null)
                return new CommandResult<>(new Message("cmd.city.perms.not-claimed", "You are not inside a city"));
        }
        else
        {
            String name = String.join(" ", cmd.args);
            city = mineCity.dataSource.getCityByName(name).orElse(null);
            if(city == null)
                return new CommandResult<>(new Message(
                        "cmd.city.perms.not-found",
                        "No city were found with name ${name}",
                        new Object[]{"name", name}
                ));
        }

        cmd.sender.send(list(city, CITY_FLAGS, "cmd.city.perms", city.getName()));
        return CommandResult.success();
    }

    @Slow
    @Async
    @Command(value = "plot.perms", args = {
        @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
        @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlot(CommandEvent cmd) throws DataSourceException
    {
        Plot plot;
        if(cmd.args.isEmpty())
        {
            if(cmd.position == null)
                return new CommandResult<>(new Message(
                        "cmd.plot.perms.type-city",
                        "Type a city name"
                ));

            plot = cmd.getChunk().getPlotAt(cmd.position.getBlock()).orElse(null);
            if(plot == null)
                return new CommandResult<>(new Message(
                        "cmd.plot.perms.not-claimed",
                        "You are not inside a plot"
                ));
        }
        else
        {
            plot = cmd.getChunk().getCity().flatMap(c-> c.getPlot(String.join(" ", cmd.args))).orElse(null);
            if(plot == null && cmd.args.size() > 1)
            {
                plot = mineCity.dataSource.getCityByName(cmd.args.get(0))
                        .flatMap(c-> c.getPlot(String.join(" ", cmd.args.subList(1, cmd.args.size()))))
                        .orElse(null)
                        ;
            }

            if(plot == null)
                return new CommandResult<>(new Message(
                        "cmd.plot.perms.not-found",
                        "No plot was found with ${search}",
                        new Object[]{"search", String.join(" ", cmd.args)}
                ));
        }

        cmd.sender.send(list(plot, PLOT_FLAGS, "cmd.plot.perms", plot.getName()));
        return CommandResult.success();
    }

    @Slow
    @Async
    @Command(value = "#model:nature.deny", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    private CommandResult<Boolean> denyNature(CommandEvent cmd, PermissionFlag flag)
    {
        Nature nature = cmd.mineCity.nature(cmd.position.world);

        if(cmd.args.isEmpty())
        {
            nature.deny(flag);

            return new CommandResult<>(new Message("cmd.nature.deny.success",
                    "The permission was denied successfully"
            ), false, true);
        }
        else
        {
            String msg = String.join(" ", cmd.args);
            nature.deny(flag, Message.string(msg));

            return new CommandResult<>(new Message("cmd.nature.deny.success.custom",
                    "<msg>The permission was prohibited with the message: <red>${msg}</red></msg>", new Object[]{"msg",msg}
            ), false, true);
        }
    }

    @Slow
    @Async
    @Command(value = "#model:city.deny", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    private CommandResult<Boolean> deny(CommandEvent cmd, PermissionFlag flag)
            throws DataSourceException
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.deny.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.deny.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(cmd.args.isEmpty())
        {
            city.deny(flag);

            return new CommandResult<>(new Message("cmd.city.deny.success",
                    "The permission was denied by default successfully"
            ), false, true);
        }
        else if(cmd.args.size() == 1)
        {
            String playerName = cmd.args.get(0);
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                Optional<City> optCity = mineCity.dataSource.getCityByName(playerName);
                if(optCity.isPresent())
                    return new CommandResult<>(new Message("cmd.city.deny.got-city-expected-player",
                            "You've typed a city name, type a player name instead to prohibit a specific player or type a " +
                                    "group name after the city name to prohibit that specific group. Do not use spaces in the names."
                    ));

                return new CommandResult<>(new Message("cmd.city.deny.player-not-found",
                        "No player was found with name ${name}", new Object[]{"name",playerName}
                ));
            }

            PlayerID player = opt.get();
            city.deny(flag, player);

            return new CommandResult<>(new Message("cmd.city.deny.success.player",
                    "The player ${player} was prohibited successfully",
                    new Object[]{"player", player.getName()}
            ), true, true);
        }
        else
        {
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(cmd.args.get(0));
            Optional<Group> groupOpt = cityOpt.map(c-> city.getGroup(cmd.args.get(1)));
            Optional<PlayerID> playerOpt = groupOpt.isPresent()? Optional.empty() : mineCity.findPlayer(cmd.args.get(0));

            if(groupOpt.isPresent())
            {
                Group group = groupOpt.get();

                if(cmd.args.size() == 2)
                    city.deny(flag, group.getIdentity());
                else
                {
                    String reason = String.join(" ", cmd.args.subList(2, cmd.args.size()));
                    city.deny(flag, group.getIdentity(), Message.string(reason));
                }

                return new CommandResult<>(new Message("cmd.city.deny.success.group",
                        "The permission was prohibited to the group ${group} from ${home} successfully",
                        new Object[][]{{"home", group.home.getName()},{"group",group.getName()}}
                ), true, true);
            }

            if(playerOpt.isPresent())
            {
                PlayerID player = playerOpt.get();

                String reason = String.join(" ", cmd.args.subList(1, cmd.args.size()));
                city.deny(flag, player, Message.string(reason));

                return new CommandResult<>(new Message("cmd.city.deny.success.player",
                        "The player ${player} was prohibited successfully",
                        new Object[]{"player", player.getName()}
                ), true, true);
            }

            if(cityOpt.isPresent())
                return new CommandResult<>(new Message("cmd.city.deny.group-not-found",
                        "The city ${city} does not have a group named ${group}",
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",cmd.args.get(1)}}
                ));

            String msg = String.join(" ", cmd.args);
            city.deny(flag, Message.string(msg));
            return new CommandResult<>(new Message("cmd.city.deny.success.custom",
                    "<msg>The permission was prohibited by default with the message: <red>${msg}</red></msg>", new Object[]{"msg",msg}
            ), false, true);
        }
    }

    @Slow
    @Async
    @Command(value = "#model:nature.allow", console = false)
    private CommandResult<?> allowNature(CommandEvent cmd, PermissionFlag flag)
    {
        Nature nature = cmd.mineCity.nature(cmd.position.world);

        nature.allow(flag);

        return new CommandResult<>(new Message("cmd.city.allow.success",
                "The permission was granted successfully"
        ), true, true);
    }

    @Slow
    @Async
    @Command(value = "#model:city.allow", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    private CommandResult<?> allow(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.allow.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.allow.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(cmd.args.isEmpty())
        {
            city.allow(flag);

            return new CommandResult<>(new Message("cmd.city.allow.success",
                    "The permission was granted by default successfully"
            ), true, true);
        }
        else if(cmd.args.size() == 1)
        {
            String playerName = cmd.args.get(0);
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                Optional<City> optCity = mineCity.dataSource.getCityByName(playerName);
                if(optCity.isPresent())
                    return new CommandResult<>(new Message("cmd.city.allow.got-city-expected-player",
                            "You've typed a city name, type a player name instead to allow a specific player or type a " +
                                    "group name after the city name to allow that specific group. Do not use spaces in the names."
                    ));

                return new CommandResult<>(new Message("cmd.city.allow.player-not-found",
                        "No player was found with name ${name}", new Object[]{"name",playerName}
                ));
            }

            PlayerID player = opt.get();
            city.allow(flag, player);

            return new CommandResult<>(new Message("cmd.city.allow.success.player",
                    "The permission was granted to ${player} successfully",
                    new Object[]{"player", player.getName()}
            ), true, true);
        }
        else
        {
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(cmd.args.get(0));
            Optional<Group> groupOpt = cityOpt.map(c-> city.getGroup(cmd.args.get(1)));

            if(cmd.args.size() > 2)
                return new CommandResult<>(new Message("cmd.city.allow.too-many-args",
                        "You've typed too many arguments, you can't give reason messages when allowing somebody."
                ));

            if(groupOpt.isPresent())
            {
                Group group = groupOpt.get();

                city.allow(flag, group.getIdentity());

                return new CommandResult<>(new Message("cmd.city.allow.success.group",
                        "The permission was granted to the group ${group} from ${home} successfully",
                        new Object[][]{{"home", group.home.getName()},{"group",group.getName()}}
                ), true, true);
            }

            if(cityOpt.isPresent())
                return new CommandResult<>(new Message("cmd.city.allow.group-not-found",
                        "The city ${city} does not have a group named ${group}",
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",cmd.args.get(1)}}
                ));

            return new CommandResult<>(new Message("cmd.city.allow.city-not-found",
                    "No city was found with name ${name}", new Object[]{"name",cmd.args.get(0)}
            ));
        }
    }

    @Slow
    @Async
    @Command(value = "#model:city.clear", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    private CommandResult<?> reset(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
        City city = cmd.getChunk().getCity().orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.clear.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.clear.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city", city.getName()}
            ));

        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.city.clear.no-args",
                    "Type a player name or a group name followed by a city name if the group is from a different city"
            ));

        if(cmd.args.size() == 1)
        {
            String playerName = cmd.args.get(0);
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                Optional<City> optCity = mineCity.dataSource.getCityByName(playerName);
                if(optCity.isPresent())
                    return new CommandResult<>(new Message("cmd.city.clear.got-city-expected-player",
                            "You've typed a city name, type a player name instead to reset a specific player or type a " +
                                    "group name after the city name to reset that specific group. Do not use spaces in the names."
                    ));

                return new CommandResult<>(new Message("cmd.city.clear.player-not-found",
                        "No player was found with name ${name}", new Object[]{"name",playerName}
                ));
            }

            PlayerID player = opt.get();
            city.reset(flag, player);

            return new CommandResult<>(new Message("cmd.city.clear.success.player",
                    "The direct permission was removed from ${player} successfully, the default permission will be applied now.",
                    new Object[]{"player", player.getName()}
            ), true, true);
        }
        else
        {
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(cmd.args.get(0));
            Optional<Group> groupOpt = cityOpt.map(c-> city.getGroup(cmd.args.get(1)));

            if(cmd.args.size() > 2)
                return new CommandResult<>(new Message("cmd.city.clear.too-many-args",
                        "You've typed too many arguments, you can't give reason messages when resetting somebody's permission."
                ));

            if(groupOpt.isPresent())
            {
                Group group = groupOpt.get();

                city.reset(flag, group.getIdentity());

                return new CommandResult<>(new Message("cmd.city.clear.success.group",
                        "The direct permission was removed from the the group ${group} from ${home} successfully, the default permission will be applied now.",
                        new Object[][]{{"home", group.home.getName()},{"group",group.getName()}}
                ), true, true);
            }

            if(cityOpt.isPresent())
                return new CommandResult<>(new Message("cmd.city.clear.group-not-found",
                        "The city ${city} does not have a group named ${group}",
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",cmd.args.get(1)}}
                ));

            return new CommandResult<>(new Message("cmd.city.clear.city-not-found",
                    "No city was found with name ${name}", new Object[]{"name",cmd.args.get(0)}
            ));
        }
    }

    @Slow
    @Async
    @Command(value = "#model:city.deny.all", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    private CommandResult<?> denyAll(CommandEvent cmd, PermissionFlag flag)
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.deny.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.deny.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(cmd.args.isEmpty())
            city.denyAll(flag);
        else
        {
            String reason = String.join(" ", cmd.args);
            city.denyAll(flag, Message.string(reason));
        }

        return new CommandResult<>(new Message("cmd.city.deny.success",
                "The permission was revoked successfully"), false, true);
    }

    @Slow
    @Async
    @Command(value = "#model:city.allow.all", console = false)
    private CommandResult<?> allowAll(CommandEvent cmd, PermissionFlag flag)
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.allow.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.allow.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(!cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.city.allow.all.args.count",
                    "This command does not expect extra arguments, are you sure that you want to allow everybody?"
            ));

        city.allowAll(flag);

        return new CommandResult<>(new Message("cmd.city.allow.success",
                "The permission was granted successfully"), true, true);
    }

    @Slow
    @Async
    @Command(value = "#model:city.reset.all", console = false)
    private CommandResult<?> resetAll(CommandEvent cmd, PermissionFlag flag)
    {
        City city = cmd.getChunk().getCity().orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.clear.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.clear.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(!cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.city.clear.all.args.count",
                    "This command does not expect extra arguments, are you sure that you want to reset everybody's permissions?"
            ));

        city.resetAll(flag);

        return new CommandResult<>(new Message("cmd.city.clear.success",
                "The direct permissions were removed successfully. The default permission will be applied now."), true, true);
    }

    @Slow
    @Async
    @Command(value = "#model:plot.deny", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    private CommandResult<Boolean> denyPlot(CommandEvent cmd, PermissionFlag flag)
            throws DataSourceException
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        Plot plot = mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.deny.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.deny.no-permission",
                    "You are not allowed to change the ${plot}'s permissions",
                    new Object[]{"plot",plot.getName()}
            ));

        if(cmd.args.isEmpty())
        {
            plot.deny(flag);

            return new CommandResult<>(new Message("cmd.plot.deny.success",
                    "The permission was denied by default successfully"
            ), false, true);
        }
        else if(cmd.args.size() == 1)
        {
            String playerName = cmd.args.get(0);
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                Optional<City> optCity = mineCity.dataSource.getCityByName(playerName);
                if(optCity.isPresent())
                    return new CommandResult<>(new Message("cmd.plot.deny.got-city-expected-player",
                            "You've typed a city name, type a player name instead to allow a specific player or type a " +
                                    "group name after the city name to prohibit that specific group. Do not use spaces in the names."
                    ));

                return new CommandResult<>(new Message("cmd.plot.deny.player-not-found",
                        "No player was found with name ${name}", new Object[]{"name",playerName}
                ));
            }

            PlayerID player = opt.get();
            plot.deny(flag, player);

            return new CommandResult<>(new Message("cmd.plot.deny.success.player",
                    "The player ${player} was prohibited successfully",
                    new Object[]{"player", player.getName()}
            ), true, true);
        }
        else
        {
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(cmd.args.get(0));
            Optional<Group> groupOpt = cityOpt.map(c-> plot.getCity().getGroup(cmd.args.get(1)));
            Optional<PlayerID> playerOpt = groupOpt.isPresent()? Optional.empty() : mineCity.findPlayer(cmd.args.get(0));

            if(groupOpt.isPresent())
            {
                Group group = groupOpt.get();

                if(cmd.args.size() == 2)
                    plot.deny(flag, group.getIdentity());
                else
                {
                    String reason = String.join(" ", cmd.args.subList(2, cmd.args.size()));
                    plot.deny(flag, group.getIdentity(), Message.string(reason));
                }

                return new CommandResult<>(new Message("cmd.plot.allow.success.group",
                        "The permission was granted to the group ${group} from ${home} successfully",
                        new Object[][]{{"home", group.home.getName()},{"group",group.getName()}}
                ), true, true);
            }

            if(playerOpt.isPresent())
            {
                PlayerID player = playerOpt.get();

                String reason = String.join(" ", cmd.args.subList(1, cmd.args.size()));
                plot.deny(flag, player, Message.string(reason));

                return new CommandResult<>(new Message("cmd.plot.deny.success.player",
                        "The player ${player} was prohibited successfully",
                        new Object[]{"player", player.getName()}
                ), true, true);
            }

            if(cityOpt.isPresent())
                return new CommandResult<>(new Message("cmd.plot.allow.group-not-found",
                        "The city ${city} does not have a group named ${group}",
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",cmd.args.get(1)}}
                ));

            String msg = String.join(" ", cmd.args);
            plot.deny(flag, Message.string(msg));
            return new CommandResult<>(new Message("cmd.plot.deny.success.custom",
                    "<msg>The permission was prohibited by default with the message: <red>${msg}</red></msg>", new Object[]{"msg",msg}
            ), false, true);
        }
    }

    @Slow
    @Async
    @Command(value = "#model:plot.allow", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    private CommandResult<?> allowPlot(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        Plot plot = mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.allow.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.allow.no-permission",
                    "You are not allowed to change the ${plot}'s permissions",
                    new Object[]{"plot",plot.getName()}
            ));

        if(cmd.args.isEmpty())
        {
            plot.allow(flag);

            return new CommandResult<>(new Message("cmd.plot.allow.success",
                    "The permission was granted by default successfully"
            ), true, true);
        }
        else if(cmd.args.size() == 1)
        {
            String playerName = cmd.args.get(0);
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                Optional<City> optCity = mineCity.dataSource.getCityByName(playerName);
                if(optCity.isPresent())
                    return new CommandResult<>(new Message("cmd.plot.allow.got-city-expected-player",
                            "You've typed a city name, type a player name instead to allow a specific player or type a " +
                                    "group name after the city name to allow that specific group. Do not use spaces in the names."
                    ));

                return new CommandResult<>(new Message("cmd.plot.allow.player-not-found",
                        "No player was found with name ${name}", new Object[]{"name",playerName}
                ));
            }

            PlayerID player = opt.get();
            plot.allow(flag, player);

            return new CommandResult<>(new Message("cmd.plot.allow.success.player",
                    "The permission was granted to ${player} successfully",
                    new Object[]{"player", player.getName()}
            ), true, true);
        }
        else
        {
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(cmd.args.get(0));
            Optional<Group> groupOpt = cityOpt.map(c-> plot.getCity().getGroup(cmd.args.get(1)));

            if(cmd.args.size() > 2)
                return new CommandResult<>(new Message("cmd.plot.allow.too-many-args",
                        "You've typed too many arguments, you can't give reason messages when allowing somebody."
                ));

            if(groupOpt.isPresent())
            {
                Group group = groupOpt.get();

                plot.allow(flag, group.getIdentity());

                return new CommandResult<>(new Message("cmd.plot.allow.success.group",
                        "The permission was granted to the group ${group} from ${home} successfully",
                        new Object[][]{{"home", group.home.getName()},{"group",group.getName()}}
                ), true, true);
            }

            if(cityOpt.isPresent())
                return new CommandResult<>(new Message("cmd.plot.allow.group-not-found",
                        "The city ${city} does not have a group named ${group}",
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",cmd.args.get(1)}}
                ));

            return new CommandResult<>(new Message("cmd.plot.allow.city-not-found",
                    "No city was found with name ${name}", new Object[]{"name",cmd.args.get(0)}
            ));
        }
    }

    @Slow
    @Async
    @Command(value = "#model:plot.reset", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    private CommandResult<?> resetPlot(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
        Plot plot = mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.clear.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.clear.no-permission",
                    "You are not allowed to change the ${plot}'s permissions",
                    new Object[]{"plot",plot.getName()}
            ));

        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.plot.clear.no-args",
                    "Type a player name or a group name followed by a city name if the group is from a different city"
            ));
        else if(cmd.args.size() == 1)
        {
            String playerName = cmd.args.get(0);
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                Optional<City> optCity = mineCity.dataSource.getCityByName(playerName);
                if(optCity.isPresent())
                    return new CommandResult<>(new Message("cmd.plot.clear.got-city-expected-player",
                            "You've typed a city name, type a player name instead to reset a specific player or type a " +
                                    "group name after the city name to reset that specific group. Do not use spaces in the names."
                    ));

                return new CommandResult<>(new Message("cmd.plot.clear.player-not-found",
                        "No player was found with name ${name}", new Object[]{"name",playerName}
                ));
            }

            PlayerID player = opt.get();
            plot.reset(flag, player);

            return new CommandResult<>(new Message("cmd.plot.clear.success.player",
                    "The direct permission was removed from ${player} successfully, the default permission will be applied now.",
                    new Object[]{"player", player.getName()}
            ), true, true);
        }
        else
        {
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(cmd.args.get(0));
            Optional<Group> groupOpt = cityOpt.map(c-> plot.getCity().getGroup(cmd.args.get(1)));

            if(cmd.args.size() > 2)
                return new CommandResult<>(new Message("cmd.plot.clear.too-many-args",
                        "You've typed too many arguments, you can't give reason messages when resetting somebody's permission."
                ));

            if(groupOpt.isPresent())
            {
                Group group = groupOpt.get();

                plot.reset(flag, group.getIdentity());

                return new CommandResult<>(new Message("cmd.plot.clear.success.group",
                        "The direct permission was removed from the the group ${group} from ${home} successfully, the default permission will be applied now.",
                        new Object[][]{{"home", group.home.getName()},{"group",group.getName()}}
                ), true, true);
            }

            if(cityOpt.isPresent())
                return new CommandResult<>(new Message("cmd.plot.clear.group-not-found",
                        "The city ${city} does not have a group named ${group}",
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",cmd.args.get(1)}}
                ));

            return new CommandResult<>(new Message("cmd.plot.clear.city-not-found",
                    "No city was found with name ${name}", new Object[]{"name",cmd.args.get(0)}
            ));
        }
    }

    @Slow
    @Async
    @Command(value = "#model:plot.deny.all", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    private CommandResult<?> denyAllPlot(CommandEvent cmd, PermissionFlag flag)
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        Plot plot = mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.deny.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.deny.no-permission",
                    "You are not allowed to change the ${plot}'s permissions",
                    new Object[]{"plot",plot.getName()}
            ));

        if(cmd.args.isEmpty())
            plot.denyAll(flag);
        else
        {
            String reason = String.join(" ", cmd.args);
            plot.denyAll(flag, Message.string(reason));
        }

        return new CommandResult<>(new Message("cmd.plot.deny.success",
                "The permission was revoked successfully"), false, true);
    }

    @Slow
    @Async
    @Command(value = "#model:plot.allow.all", console = false)
    private CommandResult<?> allowAllPlot(CommandEvent cmd, PermissionFlag flag)
    {
        assert cmd.position != null && cmd.sender.getPlayerId() != null;
        Plot plot = mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.allow.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.allow.no-permission",
                    "You are not allowed to change the ${plot}'s permissions",
                    new Object[]{"plot",plot.getName()}
            ));

        if(!cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.plot.allow.all.args.count",
                    "This command does not expect extra arguments, are you sure that you want to allow everybody?"
            ));

        plot.allowAll(flag);

        return new CommandResult<>(new Message("cmd.plot.allow.success",
                "The permission was granted successfully"), true, true);
    }

    @Slow
    @Async
    @Command(value = "#model:plot.reset.all", console = false)
    private CommandResult<?> resetAllPlot(CommandEvent cmd, PermissionFlag flag)
    {
        Plot plot = mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.reset.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.reset.no-permission",
                    "You are not allowed to change the ${plot}'s permissions",
                    new Object[]{"plot",plot.getName()}
            ));

        if(!cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.plot.reset.all.args.count",
                    "This command does not expect extra arguments, are you sure that you want to reset everybody's permissions?"
            ));

        plot.resetAll(flag);

        return new CommandResult<>(new Message("cmd.plot.reset.success",
                "The direct permissions were removed successfully. The default permission will be applied now."), true, true);
    }
}
