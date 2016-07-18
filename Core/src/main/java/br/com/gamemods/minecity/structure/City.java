package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.*;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
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

public final class City
{
    @NotNull
    public final MineCity mineCity;
    private ICityStorage storage;

    /**
     * ID defined by the data source implementation, may be zero but cannot be negative
     */
    private int id;
    @NotNull
    private String name;
    private PlayerID owner;
    private BlockPos spawn;
    private Map<Integer, Island> islands = new HashMap<>(1);

    public City(@NotNull MineCity mineCity, @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn) throws IllegalArgumentException
    {
        this.mineCity = mineCity;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        ClaimedChunk other = mineCity.getChunk(spawn).orElse(null);
        if(other != null && !(other.owner instanceof Nature))
            throw new IllegalArgumentException("The chunk "+spawn.getChunk()+" is reserved to "+other.owner);
    }

    public City(@NotNull MineCity mineCity, @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn,
                Collection<Island> islands, int id, @NotNull ICityStorage storage)
    {
        this.mineCity = mineCity;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        setId(id);
        this.storage = storage;
        this.islands = new HashMap<>(islands.size());
        islands.stream().forEach(island -> this.islands.put(island.getId(), island));
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
        if(storage == null) return 1;
        return islands.values().stream().mapToInt(Island::getSizeX).sum();
    }

    public int getSizeZ()
    {
        if(storage == null) return 1;
        return islands.values().stream().mapToInt(Island::getSizeZ).sum();
    }

    public int getChunkCount()
    {
        if(storage == null) return 1;
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

    public Stream<Island> connectedIslands(ChunkPos chunk)
    {
        return Direction.cardinal.stream()
                .map((DBFunction<Direction, Optional<ClaimedChunk>>) d-> mineCity.getOrFetchChunk(chunk.add(d)))
                .filter(Optional::isPresent).map(Optional::get)
                .map(ClaimedChunk::getIsland).filter(island-> island.getCity().equals(this));
    }

    public Stream<Entry<Direction, Island>> connectedIslandsEntries(ChunkPos chunk)
    {
        return Direction.cardinal.stream()
                .map((DBFunction<Direction, Entry<Direction, Island>>)
                            d-> new SimpleImmutableEntry<>(d, mineCity.getOrFetchChunk(chunk.add(d)).map(ClaimedChunk::getIsland).orElse(null))
                )
                .filter(e-> e.getValue() != null)
                .filter(e-> this.equals(e.getValue().getCity()))
                ;
    }

    public Island claim(ChunkPos chunk, boolean createIsland)
            throws IllegalArgumentException, DataSourceException, UncheckedDataSourceException, IllegalStateException
    {
        if(storage == null)
            throw new IllegalStateException();

        if(mineCity.getOrFetchChunk(chunk).map(ClaimedChunk::getCity).equals(Optional.of(this)))
            throw new IllegalArgumentException("The chunk "+chunk+" is reserved");

        Set<Island> islands = connectedIslands(chunk).collect(Collectors.toSet());

        if(islands.isEmpty())
        {
            if(!createIsland)
                throw new IllegalArgumentException("The chunk "+chunk+" is not touching an island owned by city "+id);
            Island island = storage.createIsland(this, chunk);
            this.islands.put(island.getId(), island);
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

    public Collection<Island> disclaim(ChunkPos chunk, boolean createIslands)
            throws IllegalStateException, IllegalArgumentException, DataSourceException
    {
        if(storage == null)
            throw new IllegalStateException();

        if(islands.size() == 1 && getChunkCount() == 1)
            throw new IllegalStateException("Cannot disclaim the last city's chunk, delete the city instead");

        if(getSpawn().getChunk().equals(chunk))
            throw new IllegalArgumentException("Cannot disclaim the spawn chunk");

        Island island = mineCity.getOrFetchChunk(chunk).map(ClaimedChunk::getIsland).filter(i-> i.getCity().equals(this))
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
            groups.forEach(s-> s.forEach((DisDBConsumer<ChunkPos>) mineCity::reloadChunk));
            created.forEach(i-> this.islands.put(i.getId(), i));
            return created;
        }
    }

    public void setSpawn(BlockPos pos) throws DataSourceException,IllegalArgumentException
    {
        if(!mineCity.getOrFetchChunk(pos.getChunk()).map(c->c.owner).filter(o->o instanceof Island).map(o->(Island)o)
                .filter(i-> i.getCity().equals(this)).isPresent() )
            throw new IllegalArgumentException("The block "+pos+" is not part of the city");

        if(storage != null) storage.setSpawn(this, pos);
        this.spawn = pos;
    }

    /**
     * Changes the owner of the city and saves it immediately
     * @param owner The new owner or {@code null} for server admins
     * @throws DataSourceException If the city is registered and the change failed. The owner will not be set in this case.
     */
    public void setOwner(@Nullable PlayerID owner) throws DataSourceException
    {
        if(storage != null) storage.setOwner(this, owner);
        this.owner = owner;
    }

    /**
     * Register this city as a new city. Must not be called if this city is not new.
     * @return The city ID.
     * @throws DataSourceException If an error occurred while registering
     * @throws IllegalStateException If this city is already registered
     */
    public int create() throws DataSourceException, IllegalStateException
    {
        if(storage != null) throw new IllegalStateException("The city \""+name+"\" is already registered with ID "+id);
        CityCreationResult result = mineCity.dataSource.createCity(this);
        storage = result.storage;
        islands.put(result.island.getId(), result.island);
        return id;
    }

    /**
     * Defines the City ID, this can be done only once and should only be done by the {@link IDataSource} implementation.
     * @throws IllegalStateException If the defined ID is different then the passed ID
     * @throws IllegalArgumentException If {@code < 0}
     */
    public void setId(int id) throws IllegalStateException, IllegalArgumentException
    {
        if(id < 0)
            throw new IllegalArgumentException("id = "+id);
        if(this.id > 0 && id != this.id)
            throw new IllegalStateException("Tried to change the city's \""+name+"\" ID from "+this.id+" to "+id);

        this.id = id;
    }

    /**
     * @return The City ID or {@code 0} if it's undefined
     */
    public int getId()
    {
        if(storage == null)
            return 0;
        return id;
    }
}
