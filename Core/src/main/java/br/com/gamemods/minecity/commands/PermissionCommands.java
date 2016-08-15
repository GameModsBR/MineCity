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
import br.com.gamemods.minecity.structure.Plot;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PermissionCommands
{
    private static final EnumSet<PermissionFlag> CITY_FLAGS = EnumSet.of(
            PermissionFlag.ENTER, PermissionFlag.CLICK, PermissionFlag.PICKUP, PermissionFlag.OPEN,
            PermissionFlag.PVP, PermissionFlag.PVC, PermissionFlag.MODIFY
    );
    private static final EnumSet<PermissionFlag> PLOT_FLAGS = EnumSet.copyOf(CITY_FLAGS);

    private final MineCity mineCity;

    public PermissionCommands(MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    private Message list(ExceptFlagHolder holder, PermissionFlag flag, String prefix, String name)
    {
        Optional<Message> def = holder.can(flag);
        Map<Identity<?>, Message> exceptions = holder.getDirectPermissions(flag);
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

    private Message[] list(ExceptFlagHolder holder, Set<PermissionFlag> filter, String prefix, String name)
    {
        Message[] lines = new Message[2 + filter.size()];
        lines[0] = new Message(prefix+".header", "<msg><darkgreen>----[${name}'s Permissions]---------------------</darkgreen></msg>",
                new Object[]{"name", name}
        );

        int i = 1;
        for(PermissionFlag flag: filter)
        {
            Optional<Message> def = holder.can(flag);
            Map<Identity<?>, Message> exceptions = holder.getDirectPermissions(flag);
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

    public CommandResult<?> list(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
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

    public CommandResult<?> listPlot(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
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
    private CommandResult<Boolean> deny(CommandEvent cmd, PermissionFlag flag)
            throws DataSourceException
    {
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
                            "You've typed a city name, type a player name instead to allow a specific player or type a " +
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
                    city.deny(flag, group.getIdentity(), new Message("", reason));
                }

                return new CommandResult<>(new Message("cmd.city.allow.success.group",
                        "The permission was granted to the group ${group} from ${home} successfully",
                        new Object[][]{{"home", group.home.getName()},{"group",group.getName()}}
                ), true, true);
            }

            if(playerOpt.isPresent())
            {
                PlayerID player = playerOpt.get();

                String reason = String.join(" ", cmd.args.subList(1, cmd.args.size()));
                city.deny(flag, player, new Message("", reason));

                return new CommandResult<>(new Message("cmd.city.deny.success.player",
                        "The player ${player} was prohibited successfully",
                        new Object[]{"player", player.getName()}
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
    private CommandResult<?> allow(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
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
    private CommandResult<?> denyAll(CommandEvent cmd, PermissionFlag flag)
    {
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
            city.denyAll(flag, new Message("", reason));
        }

        return new CommandResult<>(new Message("cmd.city.deny.success",
                "The permission was revoked successfully"), false, true);
    }

    @Slow
    @Async
    private CommandResult<?> allowAll(CommandEvent cmd, PermissionFlag flag)
    {
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
    private CommandResult<Boolean> denyPlot(CommandEvent cmd, PermissionFlag flag)
            throws DataSourceException
    {
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
                    plot.deny(flag, group.getIdentity(), new Message("", reason));
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
                plot.deny(flag, player, new Message("", reason));

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

            return new CommandResult<>(new Message("cmd.plot.allow.city-not-found",
                    "No city was found with name ${name}", new Object[]{"name",cmd.args.get(0)}
            ));
        }
    }

    @Slow
    @Async
    private CommandResult<?> allowPlot(CommandEvent cmd, PermissionFlag flag) throws DataSourceException
    {
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
    private CommandResult<?> denyAllPlot(CommandEvent cmd, PermissionFlag flag)
    {
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
            plot.denyAll(flag, new Message("", reason));
        }

        return new CommandResult<>(new Message("cmd.plot.deny.success",
                "The permission was revoked successfully"), false, true);
    }

    @Slow
    @Async
    private CommandResult<?> allowAllPlot(CommandEvent cmd, PermissionFlag flag)
    {
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
    @Command(value = "city.perms.enter", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> listEnter(CommandEvent cmd) throws DataSourceException
    {
        return list(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "city.perms.click", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> listClick(CommandEvent cmd) throws DataSourceException
    {
        return list(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "city.perms.pickup", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> listPickup(CommandEvent cmd) throws DataSourceException
    {
        return list(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "city.perms.open", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> listOpen(CommandEvent cmd) throws DataSourceException
    {
        return list(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.perms.pvp", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> listPVP(CommandEvent cmd) throws DataSourceException
    {
        return list(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.perms.pvc", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> listPVC(CommandEvent cmd) throws DataSourceException
    {
        return list(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "city.perms.modify", args = @Arg(name = "city", sticky = true, optional = true, type = Arg.Type.CITY))
    public CommandResult<?> listModify(CommandEvent cmd) throws DataSourceException
    {
        return list(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "plot.perms.enter", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlotEnter(CommandEvent cmd) throws DataSourceException
    {
        return listPlot(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "plot.perms.click", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlotClick(CommandEvent cmd) throws DataSourceException
    {
        return listPlot(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "plot.perms.pickup", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlotPickup(CommandEvent cmd) throws DataSourceException
    {
        return listPlot(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "plot.perms.open", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlotOpen(CommandEvent cmd) throws DataSourceException
    {
        return listPlot(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "plot.perms.pvp", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlotPVP(CommandEvent cmd) throws DataSourceException
    {
        return listPlot(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "plot.perms.pvc", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlotPVC(CommandEvent cmd) throws DataSourceException
    {
        return listPlot(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "plot.perms.modify", args = {
            @Arg(name = "city-or-plot", optional = true, type = Arg.Type.PLOT_OR_CITY),
            @Arg(name = "plot", sticky = true, optional = true, type = Arg.Type.PLOT)
    })
    public CommandResult<?> listPlotModify(CommandEvent cmd) throws DataSourceException
    {
        return listPlot(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "city.deny.enter", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
    })
    public CommandResult<?> denyEnter(CommandEvent cmd) throws DataSourceException
    {
        return deny(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "city.deny.click", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyClick(CommandEvent cmd) throws DataSourceException
    {
        return deny(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "city.deny.pickup", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPickup(CommandEvent cmd)
            throws DataSourceException
    {
        return deny(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "city.deny.open", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyOpen(CommandEvent cmd) throws DataSourceException
    {
        return deny(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.deny.pvp", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPVP(CommandEvent cmd) throws DataSourceException
    {
        return deny(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.deny.pvc", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPVC(CommandEvent cmd) throws DataSourceException
    {
        return deny(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "city.deny.modify", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyModify(CommandEvent cmd) throws DataSourceException
    {
        return deny(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "city.allow.enter", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowEnter(CommandEvent cmd)
            throws DataSourceException
    {
        return allow(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "city.allow.click", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowClick(CommandEvent cmd)
            throws DataSourceException
    {
        return allow(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "city.allow.pickup", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPickup(CommandEvent cmd)
            throws DataSourceException
    {
        return allow(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "city.allow.open", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowOpen(CommandEvent cmd) throws DataSourceException
    {
        return allow(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.allow.pvp", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVP(CommandEvent cmd) throws DataSourceException
    {
        return allow(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.allow.pvc", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVC(CommandEvent cmd) throws DataSourceException
    {
        return allow(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "city.allow.modify", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowModify(CommandEvent cmd) throws DataSourceException
    {
        return allow(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllEnter(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.click", console = false)
    public CommandResult<?> allowAllClick(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.pickup", console = false)
    public CommandResult<?> allowAllPickup(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.open", console = false)
    public CommandResult<?> allowAllOpen(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.pvp", console = false)
    public CommandResult<?> allowAllPVP(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.pvc", console = false)
    public CommandResult<?> allowAllPVC(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.modify", console = false)
    public CommandResult<?> allowAllModify(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllEnter(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.click", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllClick(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.pickup", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPickup(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.open", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllOpen(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.pvp", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVP(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.pvc", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVC(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.modify", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllModify(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.enter", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyEnterPlot(CommandEvent cmd) throws DataSourceException
    {
        return denyPlot(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.click", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyClickPlot(CommandEvent cmd) throws DataSourceException
    {
        return denyPlot(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.pickup", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPickupPlot(CommandEvent cmd)
            throws DataSourceException
    {
        return denyPlot(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.open", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyOpenPlot(CommandEvent cmd) throws DataSourceException
    {
        return denyPlot(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.pvp", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPVPPlot(CommandEvent cmd) throws DataSourceException
    {
        return denyPlot(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.pvc", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPVCPlot(CommandEvent cmd) throws DataSourceException
    {
        return denyPlot(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.modify", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyModifyPlot(CommandEvent cmd) throws DataSourceException
    {
        return denyPlot(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.enter", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowEnterPlot(CommandEvent cmd)
            throws DataSourceException
    {
        return allowPlot(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.click", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowClickPlot(CommandEvent cmd)
            throws DataSourceException
    {
        return allowPlot(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.pickup", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPickupPlot(CommandEvent cmd)
            throws DataSourceException
    {
        return allowPlot(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.open", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowOpenPlot(CommandEvent cmd) throws DataSourceException
    {
        return allowPlot(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.pvp", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVPPlot(CommandEvent cmd) throws DataSourceException
    {
        return allowPlot(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.pvc", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVCPlot(CommandEvent cmd) throws DataSourceException
    {
        return allowPlot(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.modify", console = false,
            args = {@Arg(name = "player or city", type = Arg.Type.PLAYER_OR_CITY, optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowModifyPlot(CommandEvent cmd) throws DataSourceException
    {
        return allowPlot(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.all.enter", console = false)
    public CommandResult<?> allowAllEnterPlot(CommandEvent cmd)
    {
        return allowAllPlot(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.all.click", console = false)
    public CommandResult<?> allowAllClickPlot(CommandEvent cmd)
    {
        return allowAllPlot(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.all.pickup", console = false)
    public CommandResult<?> allowAllPickupPlot(CommandEvent cmd)
    {
        return allowAllPlot(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.all.open", console = false)
    public CommandResult<?> allowAllOpenPlot(CommandEvent cmd)
    {
        return allowAllPlot(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.all.pvp", console = false)
    public CommandResult<?> allowAllPVPPlot(CommandEvent cmd)
    {
        return allowAllPlot(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.all.pvc", console = false)
    public CommandResult<?> allowAllPVCPlot(CommandEvent cmd)
    {
        return allowAllPlot(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "plot.allow.all.modify", console = false)
    public CommandResult<?> allowAllModifyPlot(CommandEvent cmd)
    {
        return allowAllPlot(cmd, PermissionFlag.MODIFY);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllEnterPlot(CommandEvent cmd)
    {
        return denyAllPlot(cmd, PermissionFlag.ENTER);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.all.click", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllClickPlot(CommandEvent cmd)
    {
        return denyAllPlot(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.all.pickup", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPickupPlot(CommandEvent cmd)
    {
        return denyAllPlot(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.all.open", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllOpenPlot(CommandEvent cmd)
    {
        return denyAllPlot(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.all.pvp", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVPPlot(CommandEvent cmd)
    {
        return denyAllPlot(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.all.pvc", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVCPlot(CommandEvent cmd)
    {
        return denyAllPlot(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "plot.deny.all.modify", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllModifyPlot(CommandEvent cmd)
    {
        return denyAllPlot(cmd, PermissionFlag.MODIFY);
    }
}
