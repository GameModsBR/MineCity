package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.Group;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.jetbrains.annotations.NotNull;

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
    @Command(value = "group.create", console = false, args = @Arg(name = "group name", sticky = true))
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

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
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
    @Command(value = "group.add", console = false,
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

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.group.add.no-permission",
                    "You don't have permission to add players to groups from ${city}",
                    new Object[]{"city", city.getName()}
            ));

        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.add.group-not-found",
                    "The group ${group} was not found in ${city}", new Object[][]{
                    {"group", groupName}, {"city", city.getName()}
            }));

        PlayerID player = mineCity.dataSource.getPlayer(playerName).orElse(null);
        if(player == null)
            return new CommandResult<>(new Message("cmd.group.add.player-not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(group.hasMember(player))
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
    @Command(value = "group.remove", console = false,
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

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.group.remove.no-permission",
                    "You don't have permission to remove players from groups from ${city}",
                    new Object[]{"city", city.getName()}
            ));

        Group group = city.getGroup(groupName);
        if(group == null)
            return new CommandResult<>(new Message("cmd.group.remove.group-not-found",
                    "The group ${group} was not found in ${city}", new Object[][]{
                    {"group", groupName}, {"city", city.getName()}
            }));

        PlayerID player = mineCity.dataSource.getPlayer(playerName).orElse(null);
        if(player == null)
            return new CommandResult<>(new Message("cmd.group.remove.player-not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(!group.hasMember(player))
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
