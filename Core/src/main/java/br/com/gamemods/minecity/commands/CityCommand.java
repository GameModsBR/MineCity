package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.OptionalPlayer;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.economy.BalanceResult;
import br.com.gamemods.minecity.economy.OperationResult;
import br.com.gamemods.minecity.structure.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class CityCommand
{
    private static final boolean ENABLE_CACHE = true;

    @NotNull
    private final MineCity mineCity;

    public CityCommand(@NotNull MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    @Slow
    @Async
    @Command(value = "city.info", console = false, args = @Arg(name = "name", sticky = true, optional = true, type = Arg.Type.CITY))
    public static Message info(CommandEvent cmd) throws DataSourceException
    {
        City city;
        if(cmd.args.isEmpty())
        {
            city = cmd.getChunk().getCity().orElse(null);
            if(city == null)
                return new Message("cmd.city.info.not-inside-city", "You are not inside a city");
        }
        else
        {
            String name = String.join(" ", cmd.args);
            city = cmd.mineCity.dataSource.getCityByName(name).orElse(null);
            if(city == null)
                return new Message("cmd.city.info.not-found", "There are not city named ${name}",
                        new Object[]{"name", name}
            );
        }

        BlockPos spawn = city.getSpawn();
        OptionalPlayer owner = city.owner();
        int sizeZ = city.getSizeZ();
        int sizeX = city.getSizeX();
        cmd.sender.send(new Message(
                "cmd.city.info.page",
                "<msg><darkgreen>---<yellow>-=[City: ${name}]=-</yellow>------------</darkgreen><br/>\n" +
                "<aqua>Main location: </aqua><white>${spawn-world} X:${spawn-x} Y:${spawn-y} Z:${spawn-z}</white><br/>\n" +
                "<aqua>Owner: </aqua><white>${owner}</white><br/>\n" +
                "<aqua>Size: </aqua><white>${area-squared}m², X:${size-x}, Z:${size-z}</white>\n" +
                "<aqua>Islands: </aqua><white>${island-count}</white><br/>\n" +
                "<aqua>Plots: </aqua><white>${plot-count}</white><br/>\n" +
                "<aqua>Groups: </aqua><white>${group-count}</white><br/>\n" +
                "<aqua>Price: </aqua><white>${price}</white><br/>\n"+
                "<br/><darkgreen>----------------------------------</darkgreen></msg>",
                new Object[][]{
                        {"name", city.getName()},
                        {"spawn-world", spawn.world.name()},
                        {"spawn-x", spawn.x},
                        {"spawn-y", spawn.y},
                        {"spawn-z", spawn.z},
                        {"owner", owner.player() != null? owner.getName() : new Message(
                                "cmd.city.info.admin", "<msg><i>The server administrators</i></msg>"
                        )},
                        {"area-squared", sizeX * sizeZ},
                        {"size-x", sizeX},
                        {"size-z", sizeZ},
                        {"island-count", city.islands().size()},
                        {"plot-count", city.plots().count()},
                        {"group-count", city.getGroups().size()},
                        {"price", city.getPrice() < 1? new Message("cmd.city.info.not-selling", "Not for sale") : cmd.mineCity.economy.format(city.getPrice())},
                        {"location", city.can(cmd.sender, PermissionFlag.ENTER).isPresent()?
                                new Message("cmd.city.info.hidden-location", "<msg><gray>Hidden</gray></msg>") :
                                new Message("cmd.city.info.location",
                                        "<msg>${spawn-world} <aqua>X:</aqua>${spawn-x} <aqua>Y:</aqua>${spawn-y} <aqua>Z:</aqua>${spawn-z}</msg>",
                                        new Object[][]{
                                                {"spawn-world", spawn.world.name()},
                                                {"spawn-x", spawn.x},
                                                {"spawn-y", spawn.y},
                                                {"spawn-z", spawn.z}
                                        }
                                )
                        }
                }
        ));

        return null;
    }

    @Slow
    @Async
    @Command(value = "city.create", console = false, args = @Arg(name = "name", sticky = true))
    public CommandResult<City> create(CommandEvent cmd) throws DataSourceException
    {
        if(cmd.mineCity.nature(cmd.position.world).isCityCreationDenied() && !cmd.sender.hasPermission("minecity.bypass.nature.city-creation"))
            return new CommandResult<>(new Message("cmd.city.create.disabled", "City creations are disabled in ${nature}",
                    new Object[][]{
                            {"nature", cmd.position.world.name()}
                    }));

        String name = String.join(" ", cmd.args);
        String identity = identity(name);

        if(identity.isEmpty())
            return new CommandResult<>(new Message("cmd.city.create.name.empty", "Please type a city name"));

        if(identity.length() <3)
            return new CommandResult<>(new Message("cmd.city.create.name.short",
                    "The name ${name} is not valid, try a bigger name",
                    new Object[]{"name",name}
            ));

        String conflict = mineCity.dataSource.checkNameConflict(name);
        if(conflict != null)
            return new CommandResult<>(new Message("cmd.city.create.name.conflict",
                    "The name ${name} conflicts with ${conflict}",
                    new Object[][]{{"name",name},{"conflict",conflict}})
            );

        BlockPos spawn = cmd.position.getBlock();
        Optional<ClaimedChunk> optionalClaim = mineCity.getOrFetchChunk(spawn.getChunk());
        if(!optionalClaim.isPresent())
            return new CommandResult<>(new Message("cmd.city.create.chunk.not-loaded",
                    "The chunk that you are standing is not loaded properly"));

        ClaimedChunk claim = optionalClaim.get();
        Island island = claim.getIsland().orElse(null);
        if(island != null)
            return new CommandResult<>(new Message("cmd.city.create.chunk.claimed",
                    "The chunk that you are is already claimed to ${city}",
                    new Object[]{"city",island.getCity().getName()}
            ));

        City reserved = claim.getCity().orElse(null);
        if(reserved != null)
            return new CommandResult<>(new Message("cmd.city.create.chunk.reserved",
                    "The chunk that you are is reserved to ${city}", new Object[]{"city",reserved.getName()}
            ));

        PlayerID playerId = cmd.sender.getPlayerId();
        int cities = mineCity.dataSource.getCityCount(cmd.sender.getPlayerId());
        if(cities >= mineCity.limits.cities && mineCity.limits.cities > 0)
            return new CommandResult<>(new Message("cmd.city.create.limit.reached",
                    "You've reached the maximum amount of cities that you can have."
            ));

        double cost = mineCity.costs.cityCreation;
        BalanceResult balance = mineCity.economy.has(playerId, cost, spawn.world);
        if(!balance.result)
            return new CommandResult<>(new Message("cmd.city.create.economy.insufficient-funds",
                    "Insufficient funds, you need ${money} to create a city",
                    new Object[]{"money", mineCity.economy.format(cost)}
            ));

        OperationResult result = mineCity.economy.charge(cmd.sender, cost, balance, spawn.world);
        if(!result.success)
        {
            if(result.error == null)
                return new CommandResult<>(new Message("cmd.city.create.economy.error-unknown",
                        "Oopss... An unknown error has occurred while processing your transaction."
                ));
            else
                return new CommandResult<>(new Message("cmd.city.create.economy.error",
                        "The purchase has failed: ${error}",
                        new Object[]{"error", result.error}
                ));
        }

        City city;
        try
        {
            city = new City(mineCity, name, playerId, spawn, cost - result.amount);
        }
        catch(Exception e)
        {
            try
            {
                mineCity.economy.refund(playerId, cost + result.amount, null, spawn.world, true);
            }
            catch(Exception e2)
            {
                e.addSuppressed(e2);
            }

            throw e;
        }

        return new CommandResult<>(new Message("cmd.city.create.success",
                "The city ${name} was created successfully, if you get lost you can teleport back with /city spawn ${identity}",
                new Object[][]{{"name", city.getName()},{"identity",city.getIdentityName()}}
        ), city);
    }

    @Command(value = "city.delete", console = false)
    public CommandResult<?> delete(CommandEvent cmd)
    {
        City city = mineCity.getCity(cmd.position.getChunk()).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.delete.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.delete.no-permission",
                    "You don't have permission to delete the city ${city}",
                    new Object[]{"city", city.getName()}
            ));

        String code = cmd.sender.confirm(sender -> {
            city.delete();
            return new CommandResult<>(new Message("cmd.city.delete.success",
                    "The city ${city} was deleted",
                    new Object[]{"city", city.getName()}
            ), true);
        });

        return new CommandResult<>(new Message("cmd.city.delete.confirm",
                "You are about to delete the entire city ${city}, if you continue you'll delete ${islands} islands " +
                        "and you'll release all ${chunks} chunks to the nature. All groups ${groups} created on this city will " +
                        "also be deleted. There'll be no undo and this is the last warning!\n\n" +
                "Are sure about it? If you still want to delete the city, type /city confirm ${code}",
                new Object[][]{
                        {"city", city.getName()},
                        {"islands", city.islands().size()},
                        {"groups", city.getGroups().size()},
                        {"chunks", city.getChunkCount()},
                        {"code", code}
                }), true);
    }

    @Slow
    @Async
    @Command(value = "city.claim", console = false, args = @Arg(name = "city", type = Arg.Type.CITY, optional = true, sticky = true))
    public CommandResult<Island> claim(CommandEvent cmd) throws DataSourceException
    {
        PlayerID playerId = cmd.sender.getPlayerId();
        ChunkPos chunk = cmd.position.getChunk();

        if(cmd.mineCity.nature(chunk.world).isCityCreationDenied() && !cmd.sender.hasPermission("minecity.bypass.nature.city-creation"))
            return new CommandResult<>(new Message("cmd.city.claim.disabled", "You can't claim chunks from ${nature}",
                    new Object[][]{
                            {"nature", cmd.position.world.name()}
                    }));

        Optional<ClaimedChunk> claimOpt = mineCity.getChunk(chunk);
        City city = claimOpt.flatMap(ClaimedChunk::getCity).orElse(null);
        if(city != null)
            return new CommandResult<>(new Message("cmd.city.claim.already-claimed",
                    "This chunk is already claimed in name of ${city}",
                    new Object[]{"city",city.getName()}
            ));

        if(cmd.args.isEmpty())
        {
            for(Direction direction: Direction.cardinal)
            {
                ChunkPos possible = chunk.add(direction);
                City city2 = mineCity.getChunk(possible).flatMap(ClaimedChunk::getCity).orElse(null);
                if(city2 == null || city2.equals(city))
                    continue;

                if(city2.owner().equals(playerId))
                {
                    if(city == null)
                    {
                        city = city2;
                        continue;
                    }

                    if(!playerId.equals(city.owner()))
                        city = city2;
                    else
                        return new CommandResult<>(new Message("cmd.city.claim.ambiguous",
                                "Both cities ${1} and ${2} are touching this chunk, repeat the command specifying the city name.",
                                new Object[][]{{1,city.getName()}, {2,city2.getName()}}
                        ));
                }
            }

            if(city == null)
                return new CommandResult<>(new Message("cmd.city.claim.not-nearby",
                        "There're no cities touching this chunk, get closer to your city and try again. If you want to create " +
                        "an island then specify the city name, if you want to create a new city then type ${create}",
                        new Object[]{"create","/city create <name>"}
                ));
        }
        else
        {
            String name = String.join(" ", cmd.args);
            city = mineCity.dataSource.getCityByName(name).orElse(null);

            if(city == null)
                return new CommandResult<>(new Message("cmd.city.claim.not-found",
                        "There's not city named $[name}, if you want to create a new city then type ${create}",
                        new Object[]{"create","/city create <name>"}
                ));
        }

        if(claimOpt.get().reserve && claimOpt.get().getCityAcceptingReserve().get() != city)
            return new CommandResult<>(new Message("cmd.city.claim.reserved",
                    "This chunk is reserved to ${name}",
                    new Object[]{"name",claimOpt.get().getCity().get().getName()}
            ));

        if(!playerId.equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.claim.no-permission",
                    "You are not allowed to claim chunks in name of ${city}",
                    new Object[]{"city", city.getName()}
            ));

        boolean islandCreation = !cmd.args.isEmpty() && !city.connectedIslands(chunk).findAny().isPresent();
        if(islandCreation && mineCity.limits.islands > 0 && city.islands().size() >= mineCity.limits.islands)
            return new CommandResult<>(new Message("cmd.city.claim.limit.reached",
                    "The city ${city} has reached the maximum number of islands that it can have."
            ));

        double cost = islandCreation? Math.max(mineCity.costs.islandCreation, mineCity.costs.claim) : mineCity.costs.claim;
        BalanceResult balance = mineCity.economy.has(playerId, cost, chunk.world);
        if(!balance.result)
            return new CommandResult<>(new Message("cmd.city.claim.economy.insufficient-funds",
                    "Insufficient funds, you need ${money} to claim this chunk to ${city}",
                    new Object[][]{
                            {"money", mineCity.economy.format(cost)},
                            {"city", city.getName()}
                    }
            ));

        OperationResult charge = mineCity.economy.charge(cmd.sender, cost, balance, chunk.world);
        if(!charge.success)
        {
            if(charge.error == null)
                return new CommandResult<>(new Message("cmd.city.claim.economy.error-unknown",
                        "Oopss... An unknown error has occurred while processing your transaction."
                ));
            else
                return new CommandResult<>(new Message("cmd.city.claim.economy.error",
                        "The purchase has failed: ${error}",
                        new Object[]{"error", charge.error}
                ));
        }

        boolean investmentRegistered = false;
        double investment = cost - charge.amount;
        try
        {
            city.invested(investment);
            investmentRegistered = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        Island claim;
        try
        {
            claim = city.claim(chunk, islandCreation);
        }
        catch(Throwable e)
        {
            mineCity.economy.refund(playerId, investment, balance, chunk.world, e);
            if(investmentRegistered)
            {
                try
                {
                    city.invested(-investment);
                }
                catch(Exception e2)
                {
                    e.addSuppressed(e2);
                }
            }
            throw e;
        }

        return new CommandResult<>(new Message("cmd.city.claim.success",
                "This chunk was claimed to ${city} successfully.",
                new Object[]{"city",city.getName()}
        ), claim);
    }

    @Slow
    @Async
    @Command(value = "city.disclaim", console = false)
    public CommandResult<Collection<Island>> disclaim(CommandEvent cmd)
            throws DataSourceException
    {
        ChunkPos chunk = cmd.position.getChunk();
        Optional<ClaimedChunk> claim = mineCity.getChunk(chunk);
        City city = claim.flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.disclaim.not-claimed",
                    "This chunk is not claimed by any city"
            ));

        if(!claim.get().getPlots().isEmpty())
            return new CommandResult<>(new Message("cmd.city.disclaim.contains-plots",
                    "Cannot disclaim this chunk because it contains plots."
            ));

        PlayerID playerId = cmd.sender.getPlayerId();
        if(!playerId.equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.disclaim.no-permission",
                    "You are not allowed to disclaim a chunk owned by ${city}",
                    new Object[]{"city",city.getName()}
            ));

        if(city.getChunkCount() == 1)
            return new CommandResult<>(new Message("cmd.city.disclaim.last",
                    "Cannot disclaim the last city's chunk, delete the city instead"));

        if(city.getSpawn().getChunk().equals(chunk))
            return new CommandResult<>(new Message("cmd.city.disclaim.spawn",
                    "Cannot disclaim the spawn chunk"));

        boolean createIslands = mineCity.limits.islands <= 0 || city.islands().size() < mineCity.limits.islands;
        Collection<Island> newIslands;
        try
        {
            newIslands = city.disclaim(chunk, createIslands);
        }
        catch(IllegalArgumentException e)
        {
            if(!createIslands && String.valueOf(e.getMessage()).contains("is required by other chunks"))
                newIslands = null;
            else
                throw e;
        }

        if(newIslands != null && mineCity.limits.islands > 0 && !newIslands.isEmpty() && city.islands().size() > mineCity.limits.islands)
        {
            try
            {
                city.disclaim(chunk, true);
                newIslands = null;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        if(newIslands == null)
            return new CommandResult<>(new Message("cmd.city.disclaim.limits.required",
                    "This chunk can't be disclaimed because it's required by other chunks and ${city}'s island limit would be exceeded.",
                    new Object[]{"city", city.getName()}
            ));

        int count = newIslands.size();
        if(count > 0)
        {
            double cost = mineCity.costs.islandCreation * count;
            BalanceResult balance = mineCity.economy.has(playerId, cost, chunk.world);
            if(!balance.result)
            {
                try
                {
                    city.claim(chunk, true);
                    return new CommandResult<>(new Message("cmd.city.disclaim.economy.insufficient-funds",
                            "Insufficient funds, you need ${money} to disclaim this chunk because it would create ${count} islands.",
                            new Object[][]{
                                    {"money", mineCity.economy.format(cost)},
                                    {"count", count}
                            }
                    ));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                OperationResult result = mineCity.economy.charge(cmd.sender, cost, balance, chunk.world);
                if(!result.success)
                {
                    try
                    {
                        city.claim(chunk, true);
                        if(result.error == null)
                            return new CommandResult<>(new Message("cmd.city.disclaim.economy.error-unknown",
                                    "Oopss... An unknown error has occurred while processing your transaction."
                            ));
                        else
                            return new CommandResult<>(new Message("cmd.city.disclaim.economy.error",
                                    "The purchase has failed: ${error}",
                                    new Object[]{"error", result.error}
                            ));
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try
                    {
                        city.invested(cost - result.amount);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(count == 1)
            return new CommandResult<>(new Message("cmd.city.disclaim.success",
                    "This chunk was disclaimed from ${city} successfully.",
                    new Object[]{"city",city.getName()}
            ), Collections.emptyList());
        else if(count == 2)
            return new CommandResult<>(new Message("cmd.city.disclaim.success.one-new-island",
                    "This chunk was disclaimed from ${city} successfully. One island was created as result of this disclaim.",
                    new Object[]{"city",city.getName()})
            , newIslands);
        else
            return new CommandResult<>(new Message("cmd.city.disclaim.success.n-new-islands",
                    "This chunk was disclaimed from ${city} successfully. ${count} islands were created as result of this disclaim.",
                    new Object[][]{{"city",city.getName()}, {"count", count -1}})
                    , newIslands);
    }

    @Slow
    @Async
    @Command(value = "city.spawn", console = false, args = @Arg(name = "city", type = Arg.Type.CITY, sticky = true))
    public CommandResult<Void> spawn(CommandEvent cmd)
            throws DataSourceException, InterruptedException, ExecutionException, TimeoutException
    {
        String cityName = String.join(" ", cmd.args);
        String id = identity(cityName);

        if(id.length() < 3)
            return new CommandResult<>(new Message("cmd.city.spawn.invalid-name",
                    "Please type a valid name",
                    new Object[]{"name", cityName})
            );

        City city = mineCity.dataSource.getCityByName(id).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.spawn.not-found",
                    "There're no city named ${name}",
                    new Object[]{"name",cityName})
            );

        double cost = mineCity.costs.goToCity;
        PlayerID playerId = cmd.sender.getPlayerId();
        EntityPos position = cmd.position;
        BalanceResult balance = mineCity.economy.has(playerId, cost, position.world);
        if(!balance.result)
            return new CommandResult<>(new Message("cmd.city.spawn.economy.insufficient-funds",
                    "Insufficient funds, you need ${money} to go to the city ${city}",
                    new Object[][]{
                            {"money", mineCity.economy.format(cost)},
                            {"city", city.getName()}
                    }
            ));

        OperationResult change = mineCity.economy.charge(cmd.sender, cost, balance, position.world);
        if(!change.success)
        {
            if(change.error == null)
                return new CommandResult<>(new Message("cmd.city.spawn.economy.error-unknown",
                        "Oopss... An unknown error has occurred while processing your transaction."
                ));
            else
                return new CommandResult<>(new Message("cmd.city.spawn.economy.error",
                        "The purchase has failed: ${error}",
                        new Object[]{"error", change.error}
                ));
        }

        Message error;
        try
        {
            Future<Message> future = mineCity.server.callSyncMethod(() -> cmd.sender.teleport(city.getSpawn()));
            error = future.get(10, TimeUnit.SECONDS);
            if(error == null)
                return CommandResult.success();
        }
        catch(Throwable e)
        {
            mineCity.economy.refund(playerId, cost - change.amount, balance, position.world, e);
            throw e;
        }

        mineCity.economy.refund(playerId, cost - change.amount, balance, position.world, true);
        return new CommandResult<>(error);
    }

    @Slow
    @Async
    @Command(value = "city.rename", console = false, args = @Arg(name = "new-name", sticky = true))
    public CommandResult<City> rename(CommandEvent cmd) throws DataSourceException
    {
        String cityName = String.join(" ", cmd.args).trim();
        String identity = identity(cityName);
        if(identity.isEmpty())
            return new CommandResult<>(new Message("cmd.city.rename.empty", "You need to type the new name"));

        if(identity.length()<3)
            return new CommandResult<>(new Message("cmd.city.rename.invalid",
                    "The name ${name} is invalid, try a bigger name",
                    new Object[]{"name", cityName}
            ));

        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.rename.not-claimed", "You are not inside a city"));

        String old = city.getName();
        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.rename.no-permission",
                    "You don't have permission to rename the city ${name}",
                    new Object[]{"name", old}
            ));

        if(old.equals(cityName))
            return new CommandResult<>(new Message("cmd.city.rename.same",
                    "This city is already named ${name}",
                    new Object[]{"name",cityName}
            ));

        city.setName(cityName);

        return new CommandResult<>(new Message("cmd.city.rename.success", "The city ${old} is now named ${new}",
                new Object[][]{{"old",old},{"new",city.getName()}}
        ), city);
    }

    @Slow
    @Async
    @Command(value = "city.transfer", console = false, args = @Arg(name = "player", type = Arg.Type.PLAYER))
    public CommandResult<City> transfer(CommandEvent cmd)
            throws DataSourceException
    {
        if(cmd.args.isEmpty() || cmd.args.get(0).trim().isEmpty())
            return new CommandResult<>(new Message("cmd.city.transfer.player.empty",
                    "This will transfer this city to an other player, type the player name that will be the new owner"));

        if(cmd.args.size() > 1)
            return new CommandResult<>(new Message("cmd.city.transfer.player.space-in-name",
                    "This will transfer this city to an other player, type the player name that will be the new owner, " +
                            "player names does not have spaces..."));

        String name = cmd.args.get(0).trim();
        PlayerID target = mineCity.getPlayer(name).orElse(null);
        if(target == null)
            return new CommandResult<>(new Message("cmd.city.transfer.player.not-found",
                    "The player ${name} was not found", new Object[]{"name", name}
            ));

        City city = mineCity.getChunk(cmd.position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.transfer.not-claimed", "You are not inside a city"));

        PlayerID cityOwner = city.owner().player();
        if(target.equals(cityOwner))
            return new CommandResult<>(new Message("cmd.city.transfer.already-owner",
                    "The city ${name} is already owned by ${owner}",
                    new Object[][]{{"name",city.getName()},{"owner", target.getName()}}
            ));

        if(cityOwner == null)
            return new CommandResult<>(new Message("cmd.city.transfer.adm-permission",
                    "Only the server admins con transfer the city ${name}",
                    new Object[]{"name",city.getName()}
            ));

        if(!cmd.sender.getPlayerId().equals(cityOwner))
            return new CommandResult<>(new Message("cmd.city.transfer.no-permission",
                    "Only ${owner} can transfer the city ${name}",
                    new Object[][]{{"owner", cityOwner.getName()}, {"name",city.getName()}}
            ));

        city.setOwner(target);

        return new CommandResult<>(new Message("cmd.city.transfer.success",
                "The city ${name} is now owned by ${owner}",
                new Object[][]{{"name",city.getName()},{"owner", target.getName()}}
        ), city);
    }

    @Slow
    @Async
    @Command(value = "city.setspawn", console = false)
    public CommandResult<City> setSpawn(CommandEvent cmd)
            throws DataSourceException
    {
        BlockPos position = cmd.position.getBlock();
        City city = mineCity.getChunk(position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.setspawn.not-claimed", "You are not inside a city"));

        PlayerID playerId = cmd.sender.getPlayerId();
        if(!playerId.equals(city.owner()))
            return new CommandResult<>(new Message("cmd.city.setspawn.no-permission",
                    "You are not allowed to change the ${name}'s spawn",
                    new Object[]{"name",city.getName()}
            ));

        if(position.equals(city.getSpawn()))
            return new CommandResult<>(new Message("cmd.city.setspawn.already", "The spawn is already set to that position"));

        double cost = mineCity.costs.cityChangeSpawn;
        BalanceResult balance = mineCity.economy.has(playerId, cost, position.world);
        if(!balance.result)
            return new CommandResult<>(new Message("cmd.city.setspawn.economy.insufficient-funds",
                    "Insufficient funds, you need ${money} to set the city's spawn here",
                    new Object[]{"money", mineCity.economy.format(cost)}
            ));

        OperationResult change = mineCity.economy.charge(cmd.sender, cost, balance, position.world);
        if(!change.success)
        {
            if(change.error == null)
                return new CommandResult<>(new Message("cmd.city.setspawn.economy.error-unknown",
                        "Oopss... An unknown error has occurred while processing your transaction."
                ));
            else
                return new CommandResult<>(new Message("cmd.city.setspawn.economy.error",
                        "The purchase has failed: ${error}",
                        new Object[]{"error", change.error}
                ));
        }

        try
        {
            city.setSpawn(position);
        }
        catch(Throwable e)
        {
            mineCity.economy.refund(playerId, cost - change.amount, balance, position.world, e);
            throw e;
        }

        return new CommandResult<>(new Message("cmd.city.setspawn.success",
                "The ${name}'s spawn was changed successfully",
                new Object[]{"name", city.getName()}
        ), city);
    }

    @Command(value = "city.list", args = @Arg(name = "page", type = Arg.Type.NUMBER, optional = true))
    public void list(CommandEvent cmd)
    {
        int page = 1;
        List<String> args = cmd.args instanceof ArrayList? cmd.args : new ArrayList<>(cmd.args);
        if(!args.isEmpty())
        {
            int index = args.size() - 1;
            String last = args.get(index);
            if(last.matches("^[0-9]+$"))
            {
                page = Integer.parseInt(last);
                args.remove(index);
            }
        }

        List<String> cities = mineCity.dataSource.cityNameSupplier().get().sorted(String::compareToIgnoreCase).collect(Collectors.toList());
        int pages = (int) Math.ceil(cities.size() / 8.0);
        page = Math.min(page, pages);
        int index = 8 * (page-1);

        Message[] lines = new Message[2 + Math.min(8, cities.size() - index)];
        for(int i = 1; i < lines.length-1; i++, index++)
            lines[i] = new Message("cmd.city.list.city",
                    "<msg><darkgray><![CDATA[ * ]]></darkgray><white>${city}</white></msg>",
                    new Object[]{"city", cities.get(index)}
            );

        lines[0] = new Message("cmd.city.list.header",
                "<msg><green>---<yellow>-=[Cities]=-</yellow>--------------------</green></msg>"
        );
        lines[lines.length-1] = (pages == 1)?
                new Message("cmd.city.list.footer.one-page",
                        "<msg><green>\n" +
                        "    Page <gold>1</gold>/<gold>1</gold>\n" +
                        "    <darkgreen>---</darkgreen>\n" +
                        "    Tip: Type <click><suggest cmd='/city go '/><hover><tooltip><gold>/city go</gold></tooltip><gold>/city go</gold></hover></click> to go to the city\n" +
                        "</green></msg>")
                : page == pages?
                new Message("cmd.city.list.footer.last-page",
                        "<msg><green>\n" +
                        "    Page <gold>${page}</gold>/<gold>${page}</gold>\n" +
                        "    <darkgreen>---</darkgreen>\n" +
                        "    Tip: Type <click><suggest cmd='/city go '/><hover><tooltip><gold>/city go</gold></tooltip><gold>/city go</gold></hover></click> to go to the city\n" +
                        "</green></msg>",
                        new Object[][]{
                                {"page", page}
                        })
                :
                new Message("cmd.city.list.footer.more-pages",
                            "<msg><green>\n" +
                            "    Page <gold>${page}</gold>/<gold>${total}</gold>\n" +
                            "    <darkgreen>---</darkgreen>\n" +
                            "    Next page: <hover>\n" +
                            "    <tooltip><gold>${next-page}</gold></tooltip>\n" +
                            "    <click>\n" +
                            "        <suggest cmd=\"${next-page}\"/>\n" +
                            "        <gold>${next-page}</gold>\n" +
                            "    </click></hover>\n" +
                            "</green></msg>",
                        new Object[][]{
                                {"page", page},
                                {"total", pages},
                                {"next-page", "/"+String.join(" ", cmd.path)+" "+(page + 1)}
                        }
        );

        cmd.sender.send(lines);
    }

    @Slow
    @Async
    @Command(value = "city.abort.sell", console = false)
    public static CommandResult<?> abortSell(CommandEvent cmd) throws DataSourceException
    {
        City city = cmd.getChunk().getCity().orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.abort.sell.not-claimed", "You are not inside a city"));

        PlayerID playerId = cmd.sender.getPlayerId();
        if(!city.owner().equals(playerId))
            return new CommandResult<>(new Message("cmd.city.abort.sell.no-permission",
                    "You don't have permission abort the sale of ${city}, only ${owner} can do that",
                    new Object[][]{
                            {"city", city.getName()},
                            {"owner", city.ownerName()}
                    }
            ));

        if(city.getPrice() < 1.0)
            return new CommandResult<>(new Message("cmd.city.abort.sell.not-selling",
                    "The city ${city} is not for sale",
                    new Object[]{"city", city.getName()}
            ));

        city.setPrice(0);
        return new CommandResult<>(new Message("cmd.city.abort.sell.success",
                "The city ${city} is no longer for sale",
                new Object[]{"city", city.getName()}
        ));
    }

    @Command(value = "city.sell", console = false, args = @Arg(name = "price", type = Arg.Type.NUMBER))
    public static CommandResult<String> sell(CommandEvent cmd)
    {
        City city = cmd.getChunk().getCity().orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.sell.not-claimed", "You are not inside a city"));

        PlayerID playerId = cmd.sender.getPlayerId();
        if(!city.owner().equals(playerId))
            return new CommandResult<>(new Message("cmd.city.sell.no-permission",
                    "You don't have permission to sell the city ${city}, only ${owner} can do that",
                    new Object[][]{
                            {"city", city.getName()},
                            {"owner", city.ownerName()}
                    }
            ));

        if(city.can(PermissionFlag.ENTER).isPresent() || city.can(PermissionFlag.CLICK).isPresent())
            return new CommandResult<>(new Message("cmd.city.sell.perms",
                    "You can't sell ${city} because you need to allow players to get inside and click on things.",
                    new Object[]{"city", city.getName()}
            ));

        if(cmd.args.size() != 1)
            return new CommandResult<>(new Message("cmd.city.sell.no-args",
                    "You need to type the price..."
            ));

        double price;
        try
        {
            price = Double.parseDouble(cmd.args.get(0).replace(',','.'));
        }
        catch(NumberFormatException e)
        {
            return new CommandResult<>(new Message("cmd.city.sell.not-number", "The price needs to be a number, it can be fractional but can't have thousands separators."));
        }

        if(price < 1.0)
            return new CommandResult<>(new Message("cmd.city.sell.free",
                    "The price can't be less then ${minimum}",
                    new Object[]{"minimum", cmd.mineCity.economy.format(1.0)}
            ));

        String code = cmd.sender.confirm((sender)-> {
            city.setPrice(price);
            return new CommandResult<>(new Message("cmd.city.sell.success",
                    "The city ${city} is now for sale by ${price}. Type /city abort sell if you change your mind.",
                    new Object[][]{
                            {"city", city.getName()},
                            {"price", cmd.mineCity.economy.format(price)}
                    }
            ), true);
        });

        double from = city.getPrice();
        if(from < 1)
            return new CommandResult<>(new Message("cmd.city.sell.confirm-new-sell",
                    "You are about to sell the city ${city} by ${price}. If you are sure about it type /city confirm ${code}",
                    new Object[][]{
                            {"city", city.getName()},
                            {"code", code},
                            {"price", cmd.mineCity.economy.format(price)}
                    }
            ), code);
        else
            return new CommandResult<>(new Message("cmd.city.sell.confirm-price-change",
                    "You are about to change the price of the city ${city} from ${from} to ${to}. If you are sure about it type /city confirm ${code}",
                    new Object[][]{
                            {"city", city.getName()},
                            {"code", code},
                            {"to", cmd.mineCity.economy.format(price)},
                            {"from", cmd.mineCity.economy.format(city.getPrice())}
                    }
            ), code);
    }

    @Command(value = "city.map", console = false, args = @Arg(name = "big", type = Arg.Type.PREDEFINED, options = "big", optional = true))
    public CommandResult<?> map(CommandEvent cmd)
    {
        boolean big = !cmd.args.isEmpty();
        /* Default chat size: 53x10 , chars are not monospaced but lines are

        +--------------------------------------123456789012345+
        |1234567890123456789012345678901234567| Celestial     |
        |2        1         2         3     37| A Pretty Name |
        |3               18 20                | Test City     |
        |4                                    | Washington    |
        |5                        XX          | Ottawa        |
        |6                 ▲   XXXX          | Edmonton      |
        |7                      XX            | New York      |
        |8                                    | Philadelphia  |
        |9                                    |               |
        |0                                    |               |
        +-----------------------------------------------------+
         */
        ChunkPos chunk = cmd.position.getChunk();
        ChunkPos cursorPos = chunk;
        Optional<ClaimedChunk> claimAtPosition = mineCity.getChunk(chunk);
        City cityAtPosition = claimAtPosition.flatMap(ClaimedChunk::getCityAcceptingReserve).orElse(null);
        char cursor;
        LegacyFormat cursorColor;
        if(cityAtPosition != null && !claimAtPosition.get().reserve)
        {
            cursorColor = cityAtPosition.getColor();
            switch(cmd.position.getCardinalDirection())
            {
                case NORTH: cursor = '\u25B2'; break;
                case EAST: cursor = '\u25B6'; break;
                case SOUTH: cursor = '\u25BC'; break;
                case WEST: cursor = '\u25C0'; break;
                case NORTH_EAST: cursor = '\u25E5'; break;
                case SOUTH_EAST: cursor = '\u25E2'; break;
                case SOUTH_WEST: cursor = '\u25E3'; break;
                case NORTH_WEST: cursor = '\u25E4'; break;
                default: cursor = '\u25CF'; break;
            }
        }
        else
        {
            cursorColor = cityAtPosition == null? LegacyFormat.RED : cityAtPosition.getColor();
            switch(cmd.position.getCardinalDirection())
            {
                case NORTH: cursor = '\u25B3'; break;
                case EAST: cursor = '\u25B7'; break;
                case SOUTH: cursor = '\u25BD'; break;
                case WEST: cursor = '\u25C1'; break;
                case NORTH_EAST: cursor = '\u25F9'; break;
                case SOUTH_EAST: cursor = '\u25FF'; break;
                case SOUTH_WEST: cursor = '\u25FA'; break;
                case NORTH_WEST: cursor = '\u25F8'; break;
                default: cursor = '\u25CB'; break;
            }
        }

        char unloaded = ' ';
        char unclaimed = '\u25A1';
        char claimed = '\u25A9';
        char oneLot = '\u25A3';
        char multipleLots = '\u25A3'; //'\u25A6';
        char reserved = '\u25A1';

        // Width: 57
        // Cursor pos: 29
        // Names: 58
        int height = big? 20 : 10;
        StringBuilder[] lines = new StringBuilder[height-1];
        List<Map<String,Object>> lineArgs = new ArrayList<>();
        int cursorHeight =  lines.length / 2;
        chunk = chunk.subtract(28, cursorHeight);
        Map<City, Float> cityDistances = new HashMap<>();
        if(cityAtPosition != null)
            cityDistances.put(cityAtPosition, 0f);

        BiConsumer<City, ChunkPos> updateDistance = (city, pos) -> {
            float currentDistance = cityDistances.getOrDefault(city, Float.MAX_VALUE);
            float dist = cursorPos.distance(pos);
            if(dist < currentDistance)
                cityDistances.put(city, dist);
        };

        long time = System.currentTimeMillis();
        for(int z=0; z< lines.length; z++)
        {
            @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
            StringBuilder sb = lines[z] = new StringBuilder("<msg><![CDATA[");
            Map<String, Object> args = new HashMap<>();
            lineArgs.add(args);
            LegacyFormat current = LegacyFormat.RESET;
            City lastCity = null;
            for(int x = 0; x < 57; x++)
            {
                if(x == 28 && z == cursorHeight)
                {
                    sb.append("]]><hover><tooltip>${you}</tooltip><").append(cursorColor.tag).append('>')
                            .append(cursor)
                            .append("</").append(cursorColor.tag).append("></hover><![CDATA[").append(current);

                    if(cityAtPosition != null && !claimAtPosition.get().reserve)
                    {
                        Collection<Plot> plots = claimAtPosition.get().getPlots();
                        int size = plots.size();
                        if(size == 0)
                            args.put("you", new Message("cmd.city.map.you.city.no-plot", "<msg><red>That's you inside ${city}</red></msg>",
                                    new Object[]{"city", cursorColor+cityAtPosition.getName()}
                            ));
                        else if(size == 1)
                            args.put("you", new Message("cmd.city.map.you.city.one-plot", "<msg><red>That's you inside ${city}</red><br/><br/>You are near ${plot}</msg>",
                                    new Object[][]{
                                            {"city", cursorColor+cityAtPosition.getName()},
                                            {"plot", plots.iterator().next().getName()}
                            }));
                        else
                            args.put("you", new Message("cmd.city.map.you.city.many-plots", "<msg><red>That's you inside ${city}</red><br/><br/>You are near:<br/> - ${plots}</msg>",
                                    new Object[][]{
                                            {"city", cursorColor+cityAtPosition.getName()},
                                            {"plots", Message.list(plots.stream().map(Plot::getName).sorted().map(Message::new).toArray(Message[]::new),
                                                    new Message("cmd.city.map.you.city.many-plots-join","\n - "))
                                            }
                                    }
                            ));
                    }
                    else if(cityAtPosition != null)
                        args.put("you", new Message("cmd.city.map.you.reserved", "<msg><red>That's you!</red><br/><br/><i>This chunk is reserved to ${city}</i></msg>",
                                new Object[]{"city", cursorColor+cityAtPosition.getName()}
                        ));
                    else
                        args.put("you", new Message("cmd.city.map.you.nature", LegacyFormat.RED+"That's you!"));
                    continue;
                }

                ChunkPos pos = new ChunkPos(chunk.world, chunk.x + x, chunk.z + z);
                MapCache cache = ENABLE_CACHE? mineCity.mapCache.get(pos) : null;
                if(cache != null)
                {
                    cache.used = time;
                    if(cache.owner != null)
                    {
                        City city = cache.owner;
                        LegacyFormat color = cache.color;
                        if(lastCity != city)
                        {
                            if(lastCity != null)
                                sb.append("]]></").append(current.tag).append("></hover>");
                            else
                                sb.append("]]>");

                            sb.append("<hover><tooltip>${city-").append(city.getId()).append("}</tooltip><").append(color.tag).append("><![CDATA[");
                            args.put("city-"+city.getId(), color+city.getName());
                            lastCity = city;
                        }
                    }
                    else
                    {
                        if(lastCity != null)
                            sb.append("]]></").append(current.tag).append("></hover><![CDATA[");
                        lastCity = null;
                    }

                    if(current != cache.color)
                        sb.append(current = cache.color);
                    sb.append(cache.c);
                    if(cache.owner != null)
                        updateDistance.accept(cache.owner, pos);
                    continue;
                }

                Optional<ClaimedChunk> claim = mineCity.getChunk(pos);
                if(!claim.isPresent())
                {
                    if(lastCity != null)
                        sb.append("]]></").append(current.tag).append("></hover><![CDATA[");
                    lastCity = null;

                    sb.append(unloaded);
                    mineCity.mapService.submit(()->{
                        try
                        {
                            ClaimedChunk dbClaim = mineCity.dataSource.getCityChunk(pos);
                            if(ENABLE_CACHE)
                            {
                                if(dbClaim == null)
                                    mineCity.mapCache.put(pos, new MapCache(LegacyFormat.BLACK, unclaimed, null));
                                else
                                {
                                    City city = dbClaim.getCityAcceptingReserve().orElseGet(Inconsistency::getInconsistentCity);
                                    mineCity.mapCache.put(pos, new MapCache(city.getColor(), dbClaim.reserve?reserved:claimed, city));
                                }
                            }
                        }
                        catch(DataSourceException e)
                        {
                            e.printStackTrace();
                        }
                    });
                    continue;
                }

                Optional<Island> island = claim.flatMap(ClaimedChunk::getIslandAcceptingReserve);
                if(island.isPresent())
                {
                    City city = island.get().getCity();
                    updateDistance.accept(city, pos);

                    LegacyFormat color = city.getColor();
                    if(lastCity != city)
                    {
                        if(lastCity != null)
                            sb.append("]]></").append(current.tag).append("></hover>");
                        else
                            sb.append("]]>");

                        sb.append("<hover><tooltip>${city-").append(city.getId()).append("}</tooltip><").append(color.tag).append("><![CDATA[");
                        args.put("city-"+city.getId(), color+city.getName());
                        lastCity = city;
                    }

                    current = color;

                    ClaimedChunk cc = claim.get();
                    int plots = cc.getPlots().size();
                    char c = cc.reserve? reserved : plots == 0? claimed : plots == 1? oneLot : multipleLots;
                    sb.append(c);

                    if(ENABLE_CACHE)
                        mineCity.mapCache.put(pos, new MapCache(current, c, city));
                }
                else
                {
                    if(lastCity != null)
                        sb.append("]]></").append(current.tag).append("></hover><![CDATA[");
                    lastCity = null;

                    if(current != LegacyFormat.BLACK)
                        sb.append(current = LegacyFormat.BLACK);
                    sb.append(unclaimed);

                    if(ENABLE_CACHE)
                        mineCity.mapCache.put(pos, new MapCache(current, unclaimed, null));
                }
            }

            if(lastCity != null)
                sb.append("]]></").append(current.tag).append("></hover><![CDATA[");

            args.put("name", "");
            sb.append(LegacyFormat.DARK_GRAY).append("| ${name}]]></msg>");
        }

        int line = lines.length-1;
        for(Map.Entry<City, Float> entry : CollectionUtil.sortByValues(cityDistances))
        {
            City city = entry.getKey();
            String name = city.getName();
            Object val;
            LegacyFormat color = city.getColor();
            if(name.length() > 15)
                val = new Message("", "<msg><hover><tooltip>${name}</tooltip><reset>${short}</reset></hover></msg>",
                        new Object[][]{
                                {"short", color+name.substring(0,14)+"\u2192"},
                                {"name", color+name}
                        }
                );
            else
                val = color +name;
            lineArgs.get(line--).put("name", val);
            if(line < 0)
                break;
        }

        Message[] messages = new Message[height];
        messages[0] = new Message("cmd.city.map.header",
                "<msg><darkgray>---------------<gray>-=[Map]=-</gray>--------------¬-<gray>-=[City Names]=-</gray></darkgray></msg>"
        );
        for(int i = 0; i < lines.length; i++)
            messages[i + 1] = new Message("", lines[i].toString(), lineArgs.get(i).entrySet().stream().map(e-> new Object[]{e.getKey(), e.getValue()}).toArray(Object[][]::new));

        cmd.sender.send(messages);

        if(ENABLE_CACHE)
        {
            long cut = time - 5*60*1000L;
            mineCity.mapCache.entrySet().parallelStream().filter(e -> e.getValue().used <= cut)
                    .map(Map.Entry::getKey).forEach(mineCity.mapCache::remove);
        }

        return CommandResult.success();
    }

    public class MapCache
    {
        final LegacyFormat color;
        final char c;
        final City owner;
        long used = System.currentTimeMillis();

        public MapCache(LegacyFormat color, char c, City owner)
        {
            this.color = color;
            this.c = c;
            this.owner = owner;
        }
    }
}
