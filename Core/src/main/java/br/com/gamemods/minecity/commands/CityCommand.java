package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
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

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class CityCommand
{
    @NotNull
    private final MineCity mineCity;

    public CityCommand(@NotNull MineCity mineCity)
    {
        this.mineCity = mineCity;
    }

    @Command(value = "city.create", console = false)
    public CommandResult<City> create(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        String name = String.join(" ", args);
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

        BlockPos spawn = sender.getPosition();
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

        City city = new City(mineCity, name, sender.getPlayerId(), spawn);
        return new CommandResult<>(new Message("cmd.city.create.success",
                "The city ${name} was created successfully, if you get lost you can teleport back with /city spawn ${identity}",
                new Object[][]{{"name", city.getName()},{"identity",city.getIdentityName()}}
        ), city);
    }

    @Command(value = "city.claim", console = false)
    public CommandResult<Island> claim(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        PlayerID playerId = sender.getPlayerId();
        ChunkPos chunk = sender.getPosition().getChunk();

        City city = mineCity.getChunk(chunk).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city != null)
            return new CommandResult<>(new Message("cmd.city.claim.already-claimed",
                    "This chunk is already claimed in name of ${city}",
                    new Object[]{"city",city.getName()}
            ));

        if(args.length == 0)
        {
            for(Direction direction: Direction.cardinal)
            {
                ChunkPos possible = chunk.add(direction);
                City city2 = mineCity.getChunk(possible).flatMap(ClaimedChunk::getCity).orElse(null);
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

                    assert city.getOwner() != null;
                    if(!city.getOwner().equals(playerId))
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
            String name = String.join(" ", args);
            city = mineCity.dataSource.getCityByName(name).orElse(null);

            if(city == null)
                return new CommandResult<>(new Message("cmd.city.claim.not-found",
                        "There's not city named $[name}, if you want to create a new city then type ${create}",
                        new Object[]{"create","/city create <name>"}
                ));
        }

        if(!playerId.equals(city.getOwner()))
            return new CommandResult<>(new Message("cmd.city.claim.no-permission",
                    "You are not allowed to claim chunks in name of ${city}",
                    new Object[]{"city", city.getName()}
            ));

        Island claim = city.claim(chunk, args.length > 0);

        return new CommandResult<>(new Message("cmd.city.claim.success",
                "This chunk was claimed to ${city} successfully.",
                new Object[]{"city",city.getName()}
        ), claim);
    }

    @Command(value = "city.disclaim", console = false)
    public CommandResult<Collection<Island>> disclaim(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        ChunkPos chunk = sender.getPosition().getChunk();
        City city = mineCity.getChunk(chunk).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.disclaim.not-claimed",
                    "This chunk is not claimed by any city"
            ));

        if(!sender.getPlayerId().equals(city.getOwner()))
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

    @Command(value = "city.spawn", console = false)
    public CommandResult<Void> spawn(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        String cityName = String.join(" ", args);
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

        Message error = sender.teleport(city.getSpawn());
        if(error == null)
            return CommandResult.success();

        return new CommandResult<>(error);
    }

    @Command(value = "city.rename", console = false)
    public CommandResult<City> rename(CommandSender sender, List<String> path, String[] args) throws DataSourceException
    {
        String cityName = String.join(" ", args).trim();
        String identity = identity(cityName);
        if(identity.isEmpty())
            return new CommandResult<>(new Message("cmd.city.rename.empty", "You need to type the new name"));

        if(identity.length()<3)
            return new CommandResult<>(new Message("cmd.city.rename.invalid",
                    "The name ${name} is invalid, try a bigger name",
                    new Object[]{"name", cityName}
            ));

        City city = mineCity.getChunk(sender.getPosition().getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.rename.not-claimed", "You are not inside a city"));

        String old = city.getName();
        if(!sender.getPlayerId().equals(city.getOwner()))
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

    @Command(value = "city.transfer", console = false)
    public CommandResult<City> command(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        if(args.length == 0 || args[0].trim().isEmpty())
            return new CommandResult<>(new Message("cmd.city.transfer.player.empty",
                    "This will transfer this city to an other player, type the player name that will be the new owner"));

        if(args.length > 1)
            return new CommandResult<>(new Message("cmd.city.transfer.player.space-in-name",
                    "This will transfer this city to an other player, type the player name that will be the new owner, " +
                            "player names does not have spaces..."));

        String name = args[0].trim();
        PlayerID target = mineCity.getPlayer(name).orElse(null);
        if(target == null)
            return new CommandResult<>(new Message("cmd.city.transfer.player.not-found",
                    "The player ${name} was not found", new Object[]{"name", name}
            ));

        City city = mineCity.getChunk(sender.getPosition().getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
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

        if(!sender.getPlayerId().equals(cityOwner))
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

    @Command(value = "city.setspawn", console = false)
    public CommandResult<City> setSpawn(CommandSender sender, List<String> path, String[] args)
            throws DataSourceException
    {
        BlockPos position = sender.getPosition();
        City city = mineCity.getChunk(position.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null);
        if(city == null)
            return new CommandResult<>(new Message("cmd.city.setspawn.not-claimed", "You are not inside a city"));

        if(!sender.getPlayerId().equals(city.getOwner()))
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

    @Command(value = "city.map", console = false)
    public CommandResult<?> map(CommandSender sender, List<String> path, String[] args)
    {
        /* Default chat size: 53x10

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
        ChunkPos chunk = sender.getPosition().getChunk();
        City cityAtPosition = mineCity.getChunk(chunk).flatMap(ClaimedChunk::getCity).orElse(null);
        char cursor;
        LegacyFormat cursorColor;
        if(cityAtPosition != null)
        {
            cursorColor = cityAtPosition.getColor();
            switch(sender.getCardinalDirection())
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
            cursorColor = LegacyFormat.RED;
            switch(sender.getCardinalDirection())
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
        char claimed = '\u25A1';
        char oneLot = '\u25A3';
        char multipleLots = '\u25A6';
        char reserved = '\u25A9';

        // Width: 57
        // Cursor pos: 29
        // Names: 58
        chunk = chunk.subtract(28, 4);
        StringBuilder[] lines = new StringBuilder[9];
        Map<City, LegacyFormat> cityColors = new HashMap<>();
        long time = System.currentTimeMillis();
        for(int z=0; z< 9; z++)
        {
            @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
            StringBuilder sb = lines[z] = new StringBuilder();
            LegacyFormat current = LegacyFormat.RESET;
            for(int x = 0; x < 57; x++)
            {
                if(x == 28 && z == 4)
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
                    if(cache.owner != null && !cityColors.containsKey(cache.owner))
                        cityColors.put(cache.owner, cache.color);
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
                                mineCity.mapCache.put(pos, new MapCache(city.getColor(), claimed, city));
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
                    LegacyFormat color = cityColors.get(city);
                    if(color == null)
                        cityColors.put(city, color = city.getColor());

                    if(color != current)
                        sb.append(current = color);

                    sb.append(claimed);

                    mineCity.mapCache.put(pos, new MapCache(current, claimed, city));
                }
                else
                {
                    if(current != LegacyFormat.BLACK)
                        sb.append(current = LegacyFormat.BLACK);
                    sb.append(unclaimed);

                    mineCity.mapCache.put(pos, new MapCache(current, claimed, null));
                }
            }

            sb.append(LegacyFormat.DARK_GRAY).append("| ");
        }

        int line = 8;
        for(Map.Entry<City, LegacyFormat> entry : cityColors.entrySet())
        {
            String name = entry.getKey().getName();
            if(name.length() > 15)
                name = name.substring(0,14)+"\u2192";
            lines[line--].append(entry.getValue()).append(name);
            if(line < 0)
                break;
        }

        Message[] messages = new Message[10];
        messages[0] = new Message("cmd.city.map.header",
                "<msg><darkgray>---------------<gray>-=[Map]=-</gray>--------------¬-<gray>-=[City Names]=-</gray></darkgray></msg>"
        );
        for(int i = 0; i < lines.length; i++)
            messages[i+1] = new Message("", lines[i].toString());

        sender.send(messages);

        long cut = time - 5*60*1000L;
        mineCity.mapCache.entrySet().parallelStream().filter(e-> e.getValue().used <= cut)
                .map(Map.Entry::getKey).forEach(mineCity.mapCache::remove);

        return CommandResult.success();
    }

    public class MapCache
    {
        LegacyFormat color;
        char c;
        City owner;
        long used = System.currentTimeMillis();

        public MapCache(LegacyFormat color, char c, City owner)
        {
            this.color = color;
            this.c = c;
            this.owner = owner;
        }
    }
}
