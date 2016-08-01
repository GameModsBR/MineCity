package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Async;
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
    @Async
    private CommandResult<Boolean> deny(CommandEvent cmd, PermissionFlag flag)
            throws DataSourceException
    {
        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.deny.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.deny.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(cmd.args.isEmpty())
        {
            city.deny(flag);

            return new CommandResult<>(new Message("cmd.city.deny.success",
                    "The permission was denied by default successfully"
            ), true, false);
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

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
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

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.deny.no-permission",
                    "You are not allowed to change the ${city}'s permissions",
                    new Object[]{"city",city.getName()}
            ));

        if(cmd.args.isEmpty())
            city.denyAll(flag);
        else
        {
            @SuppressWarnings("ConfusingArgumentToVarargsMethod")
            String reason = String.join(" ", cmd.args);
            city.denyAll(flag, new Message("", reason));
        }

        return new CommandResult<>(new Message("cmd.city.deny.success",
                "The permission was revoked successfully"), true, false);
    }

    @Slow
    @Async
    private CommandResult<?> allowAll(CommandEvent cmd, PermissionFlag flag)
    {
        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.allow.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
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
    @Command(value = "city.deny.enter", console = false,
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true),
                    @Arg(name = "reason", sticky = true, optional = true)
            })
    public CommandResult<?> denyPVC(CommandEvent cmd) throws DataSourceException
    {
        return deny(cmd, PermissionFlag.PVC);
    }

    @Slow
    @Async
    @Command(value = "city.allow.enter", console = false,
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
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
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowOpen(CommandEvent cmd) throws DataSourceException
    {
        return allow(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.allow.pvp", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVP(CommandEvent cmd) throws DataSourceException
    {
        return allow(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.allow.pvc", console = false,
            args = {@Arg(name = "player or city", optional = true),
                    @Arg(name = "group name", type = Arg.Type.GROUP, relative = "player or city", optional = true)
            })
    public CommandResult<?> allowPVC(CommandEvent cmd) throws DataSourceException
    {
        return allow(cmd, PermissionFlag.PVC);
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
    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllClick(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllPickup(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllOpen(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllPVP(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.allow.all.enter", console = false)
    public CommandResult<?> allowAllPVC(CommandEvent cmd)
    {
        return allowAll(cmd, PermissionFlag.PVC);
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
    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllClick(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.CLICK);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPickup(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.PICKUP);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllOpen(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.OPEN);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVP(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.PVP);
    }

    @Slow
    @Async
    @Command(value = "city.deny.all.enter", console = false, args = @Arg(name = "reason", sticky = true, optional = true))
    public CommandResult<?> denyAllPVC(CommandEvent cmd)
    {
        return denyAll(cmd, PermissionFlag.PVC);
    }
}
