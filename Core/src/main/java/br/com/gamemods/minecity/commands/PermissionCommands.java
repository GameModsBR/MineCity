package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.Group;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PermissionCommands
{
    private final MineCity mineCity;

    public PermissionCommands(MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    @Slow
    private CommandResult<Boolean> deny(CommandSender sender, String[] args, PermissionFlag flag)
            throws DataSourceException
    {
        City city = mineCity.getChunk(sender.getPosition().getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.deny.not-claimed", "You are not inside a city"));

        if(!sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.deny.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(args.length == 0)
        {
            city.deny(flag);

            return new CommandResult<>(new Message("cmd.city.deny.success",
                    "The permission was denied by default successfully"
            ), true, false);
        }
        else if(args.length == 1)
        {
            String playerName = args[0];
            //TODO Remove this slow call
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                //TODO Remove this slow call
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
            //TODO Remove this slow call
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(args[0]);
            Optional<Group> groupOpt = cityOpt.map(c-> city.getGroup(args[1]));
            //TODO Remove this slow call
            Optional<PlayerID> playerOpt = groupOpt.isPresent()? Optional.empty() : mineCity.findPlayer(args[0]);

            if(groupOpt.isPresent())
            {
                Group group = groupOpt.get();

                if(args.length == 2)
                    city.deny(flag, group.getIdentity());
                else
                {
                    String reason = String.join(" ", Arrays.asList(args).subList(2, args.length));
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

                String reason = String.join(" ", Arrays.asList(args).subList(1, args.length));
                city.deny(flag, player, new Message("", reason));

                return new CommandResult<>(new Message("cmd.city.deny.success.player",
                        "The player ${player} was prohibited successfully",
                        new Object[]{"player", player.getName()}
                ), true, true);
            }

            if(cityOpt.isPresent())
                return new CommandResult<>(new Message("cmd.city.allow.group-not-found",
                        "The city ${city} does not have a group named ${group}",
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",args[1]}}
                ));

            return new CommandResult<>(new Message("cmd.city.allow.city-not-found",
                    "No city was found with name ${name}", new Object[]{"name",args[0]}
            ));
        }
    }

    @Slow
    private CommandResult<?> allow(CommandSender sender, String[] args, PermissionFlag flag) throws DataSourceException
    {
        City city = mineCity.getChunk(sender.getPosition().getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.allow.not-claimed", "You are not inside a city"));

        if(!sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.allow.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(args.length == 0)
        {
            city.allow(flag);

            return new CommandResult<>(new Message("cmd.city.allow.success",
                    "The permission was granted by default successfully"
            ), true, true);
        }
        else if(args.length == 1)
        {
            String playerName = args[0];
            //TODO Remove this slow call
            Optional<PlayerID> opt = mineCity.findPlayer(playerName);
            if(!opt.isPresent())
            {
                //TODO Remove this slow call
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
            //TODO Remove this slow call
            Optional<City> cityOpt = mineCity.dataSource.getCityByName(args[0]);
            Optional<Group> groupOpt = cityOpt.map(c-> city.getGroup(args[1]));

            if(args.length > 2)
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
                        new Object[][]{{"city",cityOpt.get().getName()},{"group",args[1]}}
                ));

            return new CommandResult<>(new Message("cmd.city.allow.city-not-found",
                    "No city was found with name ${name}", new Object[]{"name",args[0]}
            ));
        }
    }

    private CommandResult<?> denyAll(CommandSender sender, String[] args, PermissionFlag flag)
    {
        City city = mineCity.getChunk(sender.getPosition().getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.deny.not-claimed", "You are not inside a city"));

        if(!sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.deny.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(args.length == 0)
            city.denyAll(flag);
        else
        {
            @SuppressWarnings("ConfusingArgumentToVarargsMethod")
            String reason = String.join(" ", args);
            city.denyAll(flag, new Message("", reason));
        }

        return new CommandResult<>(new Message("cmd.city.deny.success",
                "The permission was revoked successfully"), true, false);
    }

    private CommandResult<?> allowAll(CommandSender sender, String[] args, PermissionFlag flag)
    {
        City city = mineCity.getChunk(sender.getPosition().getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.allow.not-claimed", "You are not inside a city"));

        if(!sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.allow.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(args.length > 0)
            return new CommandResult<>(new Message("cmd.city.allow.all.args.count",
                    "This command does not expect extra arguments, are you sure that you want to allow everybody?"
            ));

        city.allowAll(flag);

        return new CommandResult<>(new Message("cmd.city.allow.success",
                "The permission was granted successfully"), true, true);
    }

    @Command(value = "city.deny.enter", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
    })
    public CommandResult<?> denyEnter(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return deny(sender, args, PermissionFlag.ENTER);
    }

    @Command(value = "city.deny.click", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyClick(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return deny(sender, args, PermissionFlag.CLICK);
    }

    @Command(value = "city.deny.pickup", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPickup(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        return deny(sender, args, PermissionFlag.PICKUP);
    }

    @Command(value = "city.deny.open", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyOpen(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return deny(sender, args, PermissionFlag.OPEN);
    }

    @Command(value = "city.deny.pvp", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPVP(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return deny(sender, args, PermissionFlag.PVP);
    }

    @Command(value = "city.deny.pvc", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPVC(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return deny(sender, args, PermissionFlag.PVC);
    }

    @Command(value = "city.allow.enter", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowEnter(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        return allow(sender, args, PermissionFlag.ENTER);
    }

    @Command(value = "city.allow.click", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowClick(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        return allow(sender, args, PermissionFlag.CLICK);
    }

    @Command(value = "city.allow.pickup", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPickup(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        return allow(sender, args, PermissionFlag.PICKUP);
    }

    @Command(value = "city.allow.open", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowOpen(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return allow(sender, args, PermissionFlag.OPEN);
    }

    @Command(value = "city.allow.pvp", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVP(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return allow(sender, args, PermissionFlag.PVP);
    }

    @Command(value = "city.allow.pvc", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVC(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        return allow(sender, args, PermissionFlag.PVC);
    }

    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllEnter(CommandSender sender, List<String> path, String[] args)
    {
        return allowAll(sender, args, PermissionFlag.ENTER);
    }

    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllClick(CommandSender sender, List<String> path, String[] args)
    {
        return allowAll(sender, args, PermissionFlag.CLICK);
    }

    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllPickup(CommandSender sender, List<String> path, String[] args)
    {
        return allowAll(sender, args, PermissionFlag.PICKUP);
    }

    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllOpen(CommandSender sender, List<String> path, String[] args)
    {
        return allowAll(sender, args, PermissionFlag.OPEN);
    }

    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllPVP(CommandSender sender, List<String> path, String[] args)
    {
        return allowAll(sender, args, PermissionFlag.PVP);
    }

    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllPVC(CommandSender sender, List<String> path, String[] args)
    {
        return allowAll(sender, args, PermissionFlag.PVC);
    }

    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllEnter(CommandSender sender, List<String> path, String[] args)
    {
        return denyAll(sender, args, PermissionFlag.ENTER);
    }

    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllClick(CommandSender sender, List<String> path, String[] args)
    {
        return denyAll(sender, args, PermissionFlag.CLICK);
    }

    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPickup(CommandSender sender, List<String> path, String[] args)
    {
        return denyAll(sender, args, PermissionFlag.PICKUP);
    }

    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllOpen(CommandSender sender, List<String> path, String[] args)
    {
        return denyAll(sender, args, PermissionFlag.OPEN);
    }

    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVP(CommandSender sender, List<String> path, String[] args)
    {
        return denyAll(sender, args, PermissionFlag.PVP);
    }

    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVC(CommandSender sender, List<String> path, String[] args)
    {
        return denyAll(sender, args, PermissionFlag.PVC);
    }
}
