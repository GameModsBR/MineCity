package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.Group;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class GroupCommand
{
    @NotNull
    private final MineCity mineCity;

    public GroupCommand(@NotNull MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    @Slow
    @Async
    @Command(value = "group.delete", args = {
            @Arg(name = "city", optional = true, type = Arg.Type.GROUP_OR_CITY),
            @Arg(name = "group name", type = Arg.Type.GROUP, sticky = true, relative = "city")
    })
    public CommandResult<String> delete(CommandEvent cmd) throws DataSourceException
    {
        int groupArgIndex = 0;
        City city;
        if(cmd.args.isEmpty())
            city = cmd.position != null? mineCity.getCity(cmd.position.getChunk()).orElse(null) : null;
        else
        {
            String cityName = cmd.args.get(0);
            city = mineCity.dataSource.getCityByName(cityName).orElse(null);
            if(city == null)
            {
                city = cmd.position != null? mineCity.getCity(cmd.position.getChunk()).orElse(null) : null;
                if(city == null)
                    return new CommandResult<>(new Message("cmd.group.delete.city-not-found",
                            "The city ${city} was not found",
                            new Object[]{"city", cityName}
                    ));
            }
            else
                groupArgIndex = 1;
        }

        if(city == null)
            return new CommandResult<>(new Message("cmd.group.delete.not-claimed", "You are not inside a city"));

        if(groupArgIndex >= cmd.args.size())
            return new CommandResult<>(new Message("cmd.group.delete.no-group", "Type a group name"));

        String groupName = String.join(" ", cmd.args.subList(groupArgIndex, cmd.args.size()));
        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.delete.group-not-found",
                    "The city ${city} does not contains a group called ${group}",
                    new Object[][]{
                            {"city", city.getName()}, {"group", groupName}
                    }));

        if(!cmd.sender.getPlayerId().equals(group.home.owner()))
            return new CommandResult<>(new Message("cmd.group.delete.no-permission",
                    "You don't have permission to delete groups from ${city}",
                    new Object[]{"city",city.getName()}
            ));

        String code = cmd.sender.confirm((sender)-> {
            group.remove();
            return new CommandResult<>(new Message("cmd.group.delete.success",
                    "The group ${group} from ${city} was deleted successfully",
                    new Object[][]{
                            {"group", group.getName()}, {"city", group.home.getName()}
            }), true);
        });

        return new CommandResult<>(new Message("cmd.group.delete.confirm",
                "Are you sure that you want to delete the group ${group} from city ${city}? It contains ${count} members: ${members}\n" +
                    "If you are sure, type /group confirm ${confirm}",
                new Object[][]{
                        {"group", group.getName()}, {"city", group.home.getName()}, {"count", group.getMembers().size()},
                        {"members", groupMembers(group, "cmd.group.delete")}, {"confirm", code}
                }));
    }

    @Slow
    @Async
    @Command(value = "group.info", console = true, args = {
            @Arg(name = "city", optional = true, type = Arg.Type.GROUP_OR_CITY),
            @Arg(name = "group name", type = Arg.Type.GROUP, sticky = true, relative = "city")
    })
    public CommandResult<?> info(CommandEvent cmd) throws DataSourceException
    {
        int groupArgIndex = 0;
        City city;
        if(cmd.args.isEmpty())
            city = cmd.position != null? mineCity.getCity(cmd.position.getChunk()).orElse(null) : null;
        else
        {
            String cityName = cmd.args.get(0);
            city = mineCity.dataSource.getCityByName(cityName).orElse(null);
            if(city == null)
            {
                city = cmd.position != null? mineCity.getCity(cmd.position.getChunk()).orElse(null) : null;
                if(city == null)
                    return new CommandResult<>(new Message("cmd.group.info.city-not-found",
                            "The city ${city} was not found",
                            new Object[]{"city", cityName}
                    ));
            }
            else
                groupArgIndex = 1;
        }

        if(city == null)
            return new CommandResult<>(new Message("cmd.group.info.not-claimed", "You are not inside a city"));

        if(groupArgIndex >= cmd.args.size())
            return new CommandResult<>(new Message("cmd.group.info.no-group", "Type a group name"));

        String groupName = String.join(" ", cmd.args.subList(groupArgIndex, cmd.args.size()));
        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.info.group-not-found",
                    "The city ${city} does not contains a group called ${group}",
                    new Object[][]{
                            {"city", city.getName()}, {"group", groupName}
                    }));

        return new CommandResult<>(new Message("cmd.group.info.success",
                "Managers: ${managers}\n\nMembers: ${members}",
                new Object[][]{
                        {"members",groupMembers(group, "cmd.group.info")},
                        {"managers",groupManagers(group, "cmd.group.info.managers")}
                }
        ), null, true);
    }

    private Message groupManagers(Group g, String prefix)
    {
        Set<PlayerID> managers = g.getManagers();
        if(managers.isEmpty())
        {
            PlayerID owner = g.home.owner().player();
            if(owner != null)
                return new Message(prefix+".mayor", "This group is managed by the mayor ${mayor}",
                        new Object[]{"mayor", owner.getName()}
                );

            return new Message(prefix+".empty", "This group is managed by the server administrators");
        }

        return groupList(managers, prefix);
    }

    private Message groupMembers(Group g, String prefix)
    {
        return groupList(g.getMembers(), prefix);
    }

    private Message groupList(Collection<? extends Identity<?>> list, String prefix)
    {
        return list.isEmpty()? new Message(prefix+".empty", "This group is empty") :
                Message.list(list.stream().sorted().map(m-> {
                    switch(m.getType())
                    {
                        case PLAYER:
                            return new Message(prefix+".player", "${name}", new Object[][]{
                                    {"name",m.getName()}
                            });
                        case ENTITY:
                            return new Message(prefix+".entity", "${name}", new Object[][]{
                                    {"name",m.getName()}
                            });
                        default:
                            return new Message(prefix+".member", "${name}", new Object[][]{
                                    {"name",m.getName()}
                            });
                    }
                }).toArray(Message[]::new), new Message(prefix+".join", ", "));
    }

    @Slow
    @Async
    @Command(value = "group.list", console = true, args = @Arg(name = "city name", type = Arg.Type.CITY, optional = true, sticky = true))
    public CommandResult<Void> list(CommandEvent cmd) throws DataSourceException
    {
        City city;
        if(cmd.args.isEmpty())
            city = cmd.position != null? mineCity.getCity(cmd.position.getChunk()).orElse(null) : null;
        else
        {
            String cityName = String.join(" ", cmd.args);
            city = mineCity.dataSource.getCityByName(cityName).orElse(null);
            if(city == null)
                return new CommandResult<>(new Message("cmd.group.list.city-not-found",
                        "The city ${city} was not found",
                        new Object[]{"city",cityName}
                ));
        }

        if(city == null)
            return new CommandResult<>(new Message("cmd.group.list.not-claimed", "You are not inside a city"));

        if(city.getGroups().isEmpty())
            return new CommandResult<>(new Message("cmd.group.list.no-groups",
                    "The city ${city} does not have groups",
                    new Object[]{"city", city.getName()}
            ), null, true);

        Message groups = Message.list(city.getGroups().stream().sorted((a,b)-> a.getName().compareToIgnoreCase(b.getName()))
                .map(g-> new Message("cmd.group.list.group","${name} (${size} members)", new Object[][]{
                        {"name",g.getName()},
                        {"size",g.getMembers().size()},
                        {"home.compact", city.getName().replaceAll("\\s", "")},
                        {"members", groupMembers(g, "cmd.group.list")}
                }))
                .toArray(Message[]::new), new Message("cmd.group.list.group.join", ", "));

        return new CommandResult<>(new Message("cmd.group.list.success",
                "The city ${city} contains ${count} groups: ${groups}",
                new Object[][]{
                        {"city", city.getName()},
                        {"count", city.getGroups().size()},
                        {"groups", groups}
                }), null, true);
    }

    @Slow
    @Async
    @Command(value = "group.create", args = @Arg(name = "group name", sticky = true))
    public CommandResult<Group> create(CommandEvent cmd) throws DataSourceException
    {
        String groupName = String.join(" ", cmd.args).trim();
        String identity = identity(groupName);
        if(identity.isEmpty())
            return new CommandResult<>(new Message("cmd.group.create.empty", "You need to type a name"));

        if(identity.length()<3)
            return new CommandResult<>(new Message("cmd.group.create.invalid",
                    "The name ${name} is invalid, try a bigger name",
                    new Object[]{"name", groupName}
            ));

        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.group.create.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.group.create.no-permission",
                    "You don't have permission to create groups in name of ${city}",
                    new Object[]{"city", city.getName()}
            ));

        Group conflict = city.getGroup(identity);
        if(conflict != null)
            return new CommandResult<>(new Message("cmd.group.create.conflict",
                    "The name ${name} conflicts with ${conflict}", new Object[][]{
                    {"name", groupName}, {"conflict", conflict.getName()}
            }));

        Group group = city.createGroup(groupName);
        return new CommandResult<>(new Message("cmd.group.create.success",
                "The group ${group} were created successfully", new Object[]
                {"group", group.getName()}
        ), group);
    }

    @Slow
    @Async
    @Command(value = "group.manager.add",
            args = {@Arg(name = "player", type = Arg.Type.PLAYER), @Arg(name = "group", type = Arg.Type.GROUP, sticky = true)})
    public CommandResult<?> addManager(CommandEvent cmd) throws DataSourceException
    {
        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.group.manager.add.empty.player", "Type a player name"));

        if(cmd.args.size() == 1)
            return new CommandResult<>(new Message("cmd.group.manager.add.empty.group", "Type a group name"));

        String playerName = cmd.args.get(0);
        String groupName = String.join(" ", cmd.args.subList(1, cmd.args.size()));

        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.group.manager.add.not-claimed", "You are not inside a city"));

        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.manager.add.group-not-found",
                    "The group ${group} was not found in ${city}", new Object[][]{
                    {"group", groupName}, {"city", city.getName()}
            }));

        PlayerID player = cmd.sender.getPlayerId();
        if(!player.equals(city.owner()))
            return new CommandResult<>(new Message("cmd.group.manager.add.no-permission",
                    "You don't have permission to add managers to groups from ${city}",
                    new Object[]{"city", city.getName()}
            ));

        player = mineCity.dataSource.getPlayer(playerName).orElse(null);
        if(player == null)
            return new CommandResult<>(new Message("cmd.group.manager.add.player-not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(group.isManager(player))
            return new CommandResult<>(new Message("cmd.group.manager.add.already-manager",
                    "The player ${name} is already a manager of the group ${group}", new Object[][]{
                    {"name",player.getName()}, {"group",group.getName()}
            }));

        group.addManager(player);
        return new CommandResult<>(new Message("cmd.group.manager.add.success",
                "The player ${player} is now a manager of the group ${group}", new Object[][]{
                {"player", player.getName()}, {"group", group.getName()}
        }), group);
    }

    @Slow
    @Async
    @Command(value = "group.manager.remove",
            args = {@Arg(name = "player", type = Arg.Type.PLAYER), @Arg(name = "group", type = Arg.Type.GROUP, sticky = true)})
    public CommandResult<?> removeManager(CommandEvent cmd) throws DataSourceException
    {
        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.group.manager.remove.empty.player", "Type a player name"));

        if(cmd.args.size() == 1)
            return new CommandResult<>(new Message("cmd.group.manager.remove.empty.group", "Type a group name"));

        String playerName = cmd.args.get(0);
        String groupName = String.join(" ", cmd.args.subList(1, cmd.args.size()));

        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.group.manager.remove.not-claimed", "You are not inside a city"));

        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.manager.remove.group-not-found",
                    "The group ${group} was not found in ${city}", new Object[][]{
                    {"group", groupName}, {"city", city.getName()}
            }));

        PlayerID player = cmd.sender.getPlayerId();
        if(!player.equals(city.owner()))
            return new CommandResult<>(new Message("cmd.group.manager.remove.no-permission",
                    "You don't have permission to demote players from groups from ${city}",
                    new Object[]{"city", city.getName()}
            ));

        player = mineCity.dataSource.getPlayer(playerName).orElse(null);
        if(player == null)
            return new CommandResult<>(new Message("cmd.group.manager.remove.player-not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(!group.isManager(player))
            return new CommandResult<>(new Message("cmd.group.manager.remove.not-manager",
                    "The player ${name} is not a manager of the group ${group}", new Object[][]{
                    {"name",player.getName()}, {"group",group.getName()}
            }));

        group.removeManager(player);
        return new CommandResult<>(new Message("cmd.group.manager.remove.success",
                "The player ${player} is no longer a manager of the group ${group}", new Object[][]{
                {"player", player.getName()}, {"group", group.getName()}
        }), group);
    }

    @Slow
    @Async
    @Command(value = "group.add",
            args = {@Arg(name = "player", type = Arg.Type.PLAYER), @Arg(name = "group", type = Arg.Type.GROUP, sticky = true)})
    public CommandResult<?> add(CommandEvent cmd) throws DataSourceException
    {
        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.group.add.empty.player", "Type a player name"));

        if(cmd.args.size() == 1)
            return new CommandResult<>(new Message("cmd.group.add.empty.group", "Type a group name"));

        String playerName = cmd.args.get(0);
        String groupName = String.join(" ", cmd.args.subList(1, cmd.args.size()));

        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.group.add.not-claimed", "You are not inside a city"));

        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.add.group-not-found",
                    "The group ${group} was not found in ${city}", new Object[][]{
                    {"group", groupName}, {"city", city.getName()}
            }));

        PlayerID player = cmd.sender.getPlayerId();
        if(!player.equals(city.owner()) && !group.isManager(player))
            return new CommandResult<>(new Message("cmd.group.add.no-permission",
                    "You don't have permission to add players to groups from ${city}",
                    new Object[]{"city", city.getName()}
            ));

        player = mineCity.dataSource.getPlayer(playerName).orElse(null);
        if(player == null)
            return new CommandResult<>(new Message("cmd.group.add.player-not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(group.isMember(player))
            return new CommandResult<>(new Message("cmd.group.add.already-member",
                    "The player ${name} is already part of the group ${group}", new Object[][]{
                    {"name",player.getName()}, {"group",group.getName()}
            }));

        group.addMember(player);
        return new CommandResult<>(new Message("cmd.group.add.success",
                "The player ${player} is now part of the group ${group}", new Object[][]{
                {"player", player.getName()}, {"group", group.getName()}
        }), group);
    }

    @Slow
    @Async
    @Command(value = "group.remove",
            args = {@Arg(name = "player", type = Arg.Type.PLAYER), @Arg(name = "group", type = Arg.Type.GROUP, sticky = true)})
    public CommandResult<?> remove(CommandEvent cmd) throws DataSourceException
    {
        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.group.remove.empty.player", "Type a player name"));

        if(cmd.args.size() == 1)
            return new CommandResult<>(new Message("cmd.group.remove.empty.group", "Type a group name"));

        String playerName = cmd.args.get(0);
        String groupName = String.join(" ", cmd.args.subList(1, cmd.args.size()));

        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.group.remove.not-claimed", "You are not inside a city"));

        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.remove.group-not-found",
                    "The group ${group} was not found in ${city}", new Object[][]{
                    {"group", groupName}, {"city", city.getName()}
            }));

        PlayerID player = cmd.sender.getPlayerId();
        if(!player.equals(city.owner()) && !group.isManager(player))
            return new CommandResult<>(new Message("cmd.group.remove.no-permission",
                    "You don't have permission to remove players from groups from ${city}",
                    new Object[]{"city", city.getName()}
            ));

        player = mineCity.dataSource.getPlayer(playerName).orElse(null);
        if(player == null)
            return new CommandResult<>(new Message("cmd.group.remove.player-not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(!group.isMember(player))
            return new CommandResult<>(new Message("cmd.group.remove.not-member",
                    "The player ${name} is not a member of the group ${group}", new Object[][]{
                    {"name",player.getName()}, {"group",group.getName()}
            }));

        group.removeMember(player);
        return new CommandResult<>(new Message("cmd.group.remove.success",
                "The player ${player} is no longer part of the group ${group}", new Object[][]{
                {"player", player.getName()}, {"group", group.getName()}
        }), group);
    }
}
