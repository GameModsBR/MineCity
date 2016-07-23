package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.*;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.BasicFlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.datasource.api.*;
import br.com.gamemods.minecity.datasource.api.unchecked.DBFunction;
import br.com.gamemods.minecity.datasource.api.unchecked.DisDBConsumer;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class City extends BasicFlagHolder
{
    @NotNull
    public final MineCity mineCity;
    @NotNull
    private ICityStorage storage;

    /**
     * ID defined by the data source implementation, may be zero but cannot be negative
     */
    private int id;
    @NotNull
    private String name;
    @NotNull
    private String identityName;
    private PlayerID owner;
    private BlockPos spawn;
    private Map<Integer, Island> islands = new HashMap<>(1);

    /**
     * Create and save a city immediately
     * @param owner The city's owner or {@code null} for none
     * @param spawn The city's spawn, the chunk be claimed to this city immediately
     * @throws IllegalArgumentException If the spawn's chunk is already reserved or the city's name is invalid
     * @throws DataSourceException If a database error occurs
     */
    public City(@NotNull MineCity mineCity, @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn)
            throws IllegalArgumentException, DataSourceException
    {
        this.mineCity = mineCity;
        this.name = name;
        identityName = StringUtil.identity(name);
        this.owner = owner;
        this.spawn = spawn;
        if(identityName.length() < 3)
            throw new IllegalArgumentException("Bad name");
        String conflict = mineCity.dataSource.checkNameConflict(identityName);
        if(conflict != null)
            throw new IllegalArgumentException("The name is already taken by: "+conflict);

        ClaimedChunk other = mineCity.getChunk(spawn).orElse(null);
        if(other != null && !(other.owner instanceof Nature))
            throw new IllegalArgumentException("The chunk "+spawn.getChunk()+" is reserved to "+other.owner);

        mineCity.defaultCityFlags.forEach(this::deny);

        CityCreationResult result = mineCity.dataSource.createCity(this);
        storage = result.storage;
        islands.put(result.island.getId(), result.island);

        try
        {
            mineCity.reloadChunk(spawn.getChunk());
        }
        catch(DataSourceException e)
        {
            System.err.println("[MineCity][SQL] Exception reloading a chunk");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Constructs an instance of a city that was loaded from the database, do not use this constructor for new cities.
     */
    public City(@NotNull MineCity mineCity, @NotNull String identityName, @NotNull String name, @Nullable PlayerID owner,
                @NotNull BlockPos spawn, Collection<Island> islands, int id, @NotNull ICityStorage storage)
    {
        this.mineCity = mineCity;
        this.name = name;
        this.identityName = identityName;
        this.owner = owner;
        this.spawn = spawn;
        setId(id);
        this.storage = storage;
        this.islands = new HashMap<>(islands.size());
        islands.stream().forEach(island -> this.islands.put(island.getId(), island));
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        if(owner != null && entity.getType() == MinecraftEntity.Type.PLAYER
                && entity.getUniqueId().equals(owner.uniqueId) && action != PermissionFlag.PVP )
            return Optional.empty();

        return super.can(entity, action);
    }

    public void setName(@NotNull String name) throws IllegalArgumentException, DataSourceException
    {
        String identity = StringUtil.identity(name);
        if(identity.length() < 3)
            throw new IllegalArgumentException("Bad name");

        if(!identityName.equals(identity))
        {
            String conflict = mineCity.dataSource.checkNameConflict(identity);
            if(conflict != null)
                throw new IllegalArgumentException("The name is already taken by: "+conflict);
        }

        storage.setName(this, identity, name);
        this.identityName = identity;
        this.name = name;
    }

    @NotNull
    public String getIdentityName()
    {
        return identityName;
    }

    @Nullable
    public Island getIsland(int id)
    {
        return islands.get(id);
    }

    @NotNull
    public Collection<Island> islands()
    {
        return Collections.unmodifiableCollection(islands.values());
    }

    @Nullable
    public PlayerID getOwner()
    {
        return owner;
    }

    public int getSizeX()
    {
        return islands.values().stream().mapToInt(Island::getSizeX).sum();
    }

    public int getSizeZ()
    {
        return islands.values().stream().mapToInt(Island::getSizeZ).sum();
    }

    public int getChunkCount()
    {
        return islands.values().stream().mapToInt(Island::getChunkCount).sum();
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    public BlockPos getSpawn()
    {
        return spawn;
    }

    public Stream<Island> connectedIslands(@NotNull ChunkPos chunk)
    {
        return Direction.cardinal.stream()
                .map((DBFunction<Direction, Optional<ClaimedChunk>>) d-> mineCity.getOrFetchChunk(chunk.add(d)))
                .map(o-> o.flatMap(ClaimedChunk::getIsland).filter(i-> i.getCity().equals(this)))
                .filter(Optional::isPresent).map(Optional::get)
                ;
    }

    public Stream<Entry<Direction, Island>> connectedIslandsEntries(@NotNull ChunkPos chunk)
    {
        return Direction.cardinal.stream()
                .map((DBFunction<Direction, Entry<Direction, Island>>)
                            d-> new SimpleImmutableEntry<>(d, mineCity.getOrFetchChunk(chunk.add(d)).flatMap(ClaimedChunk::getIsland).orElse(null))
                )
                .filter(e-> e.getValue() != null)
                .filter(e-> this.equals(e.getValue().getCity()))
                ;
    }

    public Island claim(@NotNull ChunkPos chunk, boolean createIsland)
            throws IllegalArgumentException, DataSourceException, UncheckedDataSourceException, IllegalStateException
    {
        if(mineCity.getOrFetchChunk(chunk).flatMap(ClaimedChunk::getCity).equals(Optional.of(this)))
            throw new IllegalArgumentException("The chunk "+chunk+" is reserved");

        Set<Island> islands = connectedIslands(chunk).collect(Collectors.toSet());

        if(islands.isEmpty())
        {
            if(!createIsland)
                throw new IllegalArgumentException("The chunk "+chunk+" is not touching an island owned by city "+id);
            Island island = storage.createIsland(this, chunk);
            this.islands.put(island.getId(), island);
            mineCity.reloadChunk(chunk);
            return island;
        }
        else if(islands.size() == 1)
        {
            Island island = islands.iterator().next();
            storage.claim(island, chunk);
            return island;
        }
        else
        {
            Island mainIsland = storage.claim(islands, chunk);
            islands.stream().filter(island -> !island.equals(mainIsland))
                    .forEach(island -> this.islands.remove(island.getId()));
            return mainIsland;
        }
    }

    public Collection<Island> disclaim(@NotNull ChunkPos chunk, boolean createIslands)
            throws IllegalStateException, IllegalArgumentException, DataSourceException
    {
        if(islands.size() == 1 && getChunkCount() == 1)
            throw new IllegalStateException("Cannot disclaim the last city's chunk, delete the city instead");

        if(getSpawn().getChunk().equals(chunk))
            throw new IllegalArgumentException("Cannot disclaim the spawn chunk");

        Island island = mineCity.getOrFetchChunk(chunk).flatMap(ClaimedChunk::getIsland).filter(i-> i.getCity().equals(this))
                .orElseThrow(()-> new IllegalArgumentException("The chunk " + chunk + " is not owned by the city " + id));

        Map<Direction, Island> islands = connectedIslandsEntries(chunk).filter(e->e.getValue().equals(island))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if(islands.isEmpty())
        {
            storage.deleteIsland(island);
            this.islands.remove(island.getId());
            return Collections.singleton(island);
        }
        else if(islands.size() == 1)
        {
            storage.disclaim(chunk, island);
            return Collections.singleton(island);
        }
        else
        {
            IslandArea area = storage.getArea(island);
            area.setClaimed(chunk, false);
            Set<ChunkPos> touching = area.touching(chunk);
            Set<Set<ChunkPos>> groups = touching.stream().map(area::contiguous).collect(Collectors.toSet());

            if(groups.size() == 1)
            {
                storage.disclaim(chunk, island);
                return Collections.singletonList(island);
            }

            if(!createIslands)
                throw new IllegalArgumentException("The chunk "+chunk+" is required by other chunks");

            Collection<Island> created = storage.disclaim(chunk, island, groups);
            created.forEach(i-> this.islands.put(i.getId(), i));
            groups.forEach(s-> s.forEach((DisDBConsumer<ChunkPos>) mineCity::reloadChunk));
            return created;
        }
    }

    public void setSpawn(@NotNull BlockPos pos) throws DataSourceException,IllegalArgumentException
    {
        if(!mineCity.getOrFetchChunk(pos.getChunk()).map(c->c.owner).filter(o->o instanceof Island).map(o->(Island)o)
                .filter(i-> i.getCity().equals(this)).isPresent() )
            throw new IllegalArgumentException("The block "+pos+" is not part of the city");

        storage.setSpawn(this, pos);
        this.spawn = pos;
    }

    /**
     * Changes the owner of the city and saves it immediately
     * @param owner The new owner or {@code null} for server admins
     * @throws DataSourceException If the city is registered and the change failed. The owner will not be set in this case.
     */
    public void setOwner(@Nullable PlayerID owner) throws DataSourceException
    {
        storage.setOwner(this, owner);
        this.owner = owner;
    }

    /**
     * Defines the City ID, this can be done only once and should only be done by the {@link IDataSource} implementation.
     * @throws IllegalStateException If the defined ID is different then the passed ID
     * @throws IllegalArgumentException If {@code < 0}
     */
    public void setId(int id) throws IllegalStateException, IllegalArgumentException
    {
        if(id < 0 && identityName.charAt(0) != '#')
            throw new IllegalArgumentException("id = "+id);
        if(this.id > 0 && id != this.id)
            throw new IllegalStateException("Tried to change the city's \""+name+"\" ID from "+this.id+" to "+id);

        this.id = id;
    }

    /**
     * @return The City ID
     */
    public int getId()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        City city = (City) o;
        return id == city.id && identityName.equals(city.identityName);
    }

    @Override
    public int hashCode()
    {
        int result = id;
        result = 31*result + identityName.hashCode();
        return result;
    }

    public LegacyFormat getColor()
    {
        if(name.charAt(0) == '#')
            return LegacyFormat.DARK_RED;

        if(owner == null)
            return LegacyFormat.RED;

        return LegacyFormat.CITY_COLORS[id%LegacyFormat.CITY_COLORS.length];
    }
}
