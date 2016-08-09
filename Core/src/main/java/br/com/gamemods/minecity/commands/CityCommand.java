package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class CityCommand
{
    @NotNull
    private final MineCity mineCity;

    public CityCommand(@NotNull MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    @Slow
    @Async
    @Command(value = "city.create", console = false, args = @Arg(name = "name", sticky = true))
    public CommandResult<City> create(CommandEvent cmd) throws DataSourceException
    {
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
        if(island != null && !claim.reserve)
            return new CommandResult<>(new Message("cmd.city.create.chunk.claimed",
                    "The chunk that you are is already claimed to ${city}",
                    new Object[]{"city",island.getCity().getName()}
            ));

        City reserved = claim.getCity().orElse(null);
        if(reserved != null)
            return new CommandResult<>(new Message("cmd.city.create.chunk.reserved",
                    "The chunk that you are is reserved to ${city}", new Object[]{"city",reserved.getName()}
            ));

        City city = new City(mineCity, name, cmd.sender.getPlayerId(), spawn);
        return new CommandResult<>(new Message("cmd.city.create.success",
                "The city ${name} was created successfully, if you get lost you can teleport back with /city spawn ${identity}",
                new Object[][]{{"name", city.getName()},{"identity",city.getIdentityName()}}
        ), city);
    }

    @Slow
    @Async
    @Command(value = "city.delete", console = false)
    public CommandResult<?> delete(CommandEvent cmd)
    {
        City city = mineCity.getCity(cmd.position.getChunk()).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.delete.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
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
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Command(value = "city.claim", console = false, args = @Arg(name = "city", type = Arg.Type.CITY, optional = true, sticky = true))
    public CommandResult<Island> claim(CommandEvent cmd) throws DataSourceException
    {
        PlayerID playerId = cmd.sender.getPlayerId();
        ChunkPos chunk = cmd.position.getChunk();

        Optional<ClaimedChunk> claimOpt = mineCity.getChunk(chunk);
        City city = claimOpt.flatMap(ClaimedChunk::getCity).orElse(null);
        if(city != null && !claimOpt.get().reserve)
            return new CommandResult<>(new Message("cmd.city.claim.already-claimed",
                    "This chunk is already claimed in name of ${city}",
                    new Object[]{"city",city.getName()}
            ));

        if(cmd.args.isEmpty())
        {
            for(Direction direction: Direction.cardinal)
            {
                ChunkPos possible = chunk.add(direction);
                City city2 = mineCity.getChunk(possible).filter(c-> !c.reserve).flatMap(ClaimedChunk::getCity).orElse(null);
                if(city2 == null || city2.equals(city))
                    continue;

                PlayerID owner = city2.getOwner();
                if(owner == null)
                    continue;

                if(owner.equals(playerId))
                {
                    if(city == null)
                    {
                        city = city2;
                        continue;
                    }

                    if(!playerId.equals(city.getOwner()))
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

        if(claimOpt.get().reserve && claimOpt.get().getCity().get() != city)
            return new CommandResult<>(new Message("cmd.city.claim.reserved",
                    "This chunk is reserved to ${name}",
                    new Object[]{"name",claimOpt.get().getCity().get().getName()}
            ));

        if(!playerId.equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.claim.no-permission",
                    "You are not allowed to claim chunks in name of ${city}",
                    new Object[]{"city", city.getName()}
            ));

        Island claim = city.claim(chunk, !cmd.args.isEmpty());

        return new CommandResult<>(new Message("cmd.city.claim.success",
                "This chunk was claimed to ${city} successfully.",
                new Object[]{"city",city.getName()}
        ), claim);
    }

    @Slow
    @Async
    @Command(value = "city.disclaim", console = false, args = @Arg(name = "city", type = Arg.Type.CITY, optional = true, sticky = true))
    public CommandResult<Collection<Island>> disclaim(CommandEvent cmd)
            throws DataSourceException
    {
        ChunkPos chunk = cmd.position.getChunk();
        City city = mineCity.getChunk(chunk).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.disclaim.not-claimed",
                    "This chunk is not claimed by any city"
            ));

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
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

        Collection<Island> newIslands = city.disclaim(chunk, true);

        if(newIslands.size() == 1)
            return new CommandResult<>(new Message("cmd.city.disclaim.success",
                    "This chunk was disclaimed from ${city} successfully.",
                    new Object[]{"city",city.getName()}
            ), Collections.emptyList());
        else if(newIslands.size() == 2)
            return new CommandResult<>(new Message("cmd.city.disclaim.success.one-new-island",
                    "This chunk was disclaimed from ${city} successfully. One island was created as result of this disclaim.",
                    new Object[]{"city",city.getName()})
            , newIslands);
        else
            return new CommandResult<>(new Message("cmd.city.disclaim.success.n-new-islands",
                    "This chunk was disclaimed from ${city} successfully. ${count} islands were created as result of this disclaim.",
                    new Object[][]{{"city",city.getName()}, {"count",newIslands.size()-1}})
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

        Future<Message> future = mineCity.server.callSyncMethod(()-> cmd.sender.teleport(city.getSpawn()));
        Message error = future.get(10, TimeUnit.SECONDS);
        if(error == null)
            return CommandResult.success();

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
        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
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

        PlayerID cityOwner = city.getOwner();
        if(target.equals(cityOwner))
            return new CommandResult<>(new Message("cmd.city.transfer.already-owner",
                    "The city ${name} is already owned by ${owner}",
                    new Object[][]{{"name",city.getName()},{"owner",target.name}}
            ));

        if(cityOwner == null)
            return new CommandResult<>(new Message("cmd.city.transfer.adm-permission",
                    "Only the server admins con transfer the city ${name}",
                    new Object[]{"name",city.getName()}
            ));

        if(!cmd.sender.getPlayerId().equals(cityOwner))
            return new CommandResult<>(new Message("cmd.city.transfer.no-permission",
                    "Only ${owner} can transfer the city ${name}",
                    new Object[][]{{"owner", cityOwner.name}, {"name",city.getName()}}
            ));

        city.setOwner(target);

        return new CommandResult<>(new Message("cmd.city.transfer.success",
                "The city ${name} is now owned by ${owner}",
                new Object[][]{{"name",city.getName()},{"owner",target.name}}
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

        if(!cmd.sender.getPlayerId().equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.setspawn.no-permission",
                    "You are not allowed to change the ${name}'s spawn",
                    new Object[]{"name",city.getName()}
            ));

        if(position.equals(city.getSpawn()))
            return new CommandResult<>(new Message("cmd.city.setspawn.already", "The spawn is already set to that position"));

        city.setSpawn(position);

        return new CommandResult<>(new Message("cmd.city.setspawn.success",
                "The ${name}'s spawn was changed successfully",
                new Object[]{"name", city.getName()}
        ), city);
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
        City cityAtPosition = claimAtPosition.flatMap(ClaimedChunk::getCity).orElse(null);
        char cursor;
        LegacyFormat cursorColor;
        //noinspection OptionalGetWithoutIsPresent
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
        char multipleLots = '\u25A6';
        char reserved = '\u25A1';

        // Width: 57
        // Cursor pos: 29
        // Names: 58
        int height = big? 20 : 10;
        StringBuilder[] lines = new StringBuilder[height-1];
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
            StringBuilder sb = lines[z] = new StringBuilder();
            LegacyFormat current = LegacyFormat.RESET;
            for(int x = 0; x < 57; x++)
            {
                if(x == 28 && z == cursorHeight)
                {
                    if(current != cursorColor)
                        sb.append(current = cursorColor);
                    sb.append(cursor);
                    continue;
                }

                ChunkPos pos = new ChunkPos(chunk.world, chunk.x + x, chunk.z + z);
                MapCache cache = mineCity.mapCache.get(pos);
                if(cache != null)
                {
                    cache.used = time;
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
                    sb.append(unloaded);
                    mineCity.mapService.submit(()->{
                        try
                        {
                            ClaimedChunk dbClaim = mineCity.dataSource.getCityChunk(pos);
                            if(dbClaim == null)
                                mineCity.mapCache.put(pos, new MapCache(LegacyFormat.BLACK, unclaimed, null));
                            else
                            {
                                City city = dbClaim.getCity().orElseGet(Inconsistency::getInconsistentCity);
                                mineCity.mapCache.put(pos, new MapCache(city.getColor(), dbClaim.reserve?reserved:claimed, city));
                            }
                        }
                        catch(DataSourceException e)
                        {
                            e.printStackTrace();
                        }
                    });
                    continue;
                }

                Optional<Island> island = claim.flatMap(ClaimedChunk::getIsland);
                if(island.isPresent())
                {
                    City city = island.get().getCity();
                    updateDistance.accept(city, pos);

                    LegacyFormat color = city.getColor();
                    if(color != current)
                        sb.append(current = color);

                    char c = claim.get().reserve? reserved : claimed;
                    sb.append(c);

                    mineCity.mapCache.put(pos, new MapCache(current, c, city));
                }
                else
                {
                    if(current != LegacyFormat.BLACK)
                        sb.append(current = LegacyFormat.BLACK);
                    sb.append(unclaimed);

                    mineCity.mapCache.put(pos, new MapCache(current, unclaimed, null));
                }
            }

            sb.append(LegacyFormat.DARK_GRAY).append("| ");
        }

        int line = lines.length-1;
        for(Map.Entry<City, Float> entry : CollectionUtil.sortByValues(cityDistances))
        {
            City city = entry.getKey();
            String name = city.getName();
            if(name.length() > 15)
                name = name.substring(0,14)+"\u2192";
            lines[line--].append(city.getColor()).append(name);
            if(line < 0)
                break;
        }

        Message[] messages = new Message[height];
        messages[0] = new Message("cmd.city.map.header",
                "<msg><darkgray>---------------<gray>-=[Map]=-</gray>--------------¬-<gray>-=[City Names]=-</gray></darkgray></msg>"
        );
        for(int i = 0; i < lines.length; i++)
            messages[i+1] = new Message("", lines[i].toString());

        cmd.sender.send(messages);

        long cut = time - 5*60*1000L;
        mineCity.mapCache.entrySet().parallelStream().filter(e-> e.getValue().used <= cut)
                .map(Map.Entry::getKey).forEach(mineCity.mapCache::remove);

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
