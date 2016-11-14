package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.datasource.api.*;
import br.com.gamemods.minecity.datasource.api.unchecked.DBFunction;
import br.com.gamemods.minecity.datasource.api.unchecked.DisDBConsumer;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import br.com.gamemods.minecity.economy.Tax;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public final class City extends ExceptStoredHolder
{
    public static final Message INCONSISTENT_CITY_MESSAGE = new Message("inconsistent.city", "This city is inconsistent.");

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

    @NotNull
    private OptionalPlayer owner;

    @NotNull
    private BlockPos spawn;

    @NotNull
    private final Map<Integer, Island> islands;

    @NotNull
    private final Map<String, Group> groups;

    private boolean invalid;

    private Message ownerNameCache;
    private byte ownerNameLife = Byte.MAX_VALUE;

    @NotNull
    private Tax appliedTax;

    private double investment;

    private double price;

    /**
     * Create and save a city immediately
     * @param owner The city's owner
     * @param spawn The city's spawn, the chunk be claimed to this city immediately
     * @throws IllegalArgumentException If the spawn's chunk is already reserved or the city's name is invalid
     * @throws DataSourceException If a database error occurs
     */
    @Slow
    public City(@NotNull MineCity mineCity, @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn, double investment)
            throws IllegalArgumentException, DataSourceException
    {
        this.mineCity = mineCity;
        this.investment = investment;
        this.name = name;
        identityName = identity(name);
        appliedTax = mineCity.costs.cityTaxApplied;
        this.owner = owner == null? new AdminCity(this) : owner;
        this.spawn = spawn;
        if(identityName.length() < 3)
            throw new IllegalArgumentException("Bad name");
        String conflict = mineCity.dataSource.checkNameConflict(identityName);
        if(conflict != null)
            throw new IllegalArgumentException("The name is already taken by: "+conflict);

        ClaimedChunk other = mineCity.getChunk(spawn).orElse(null);
        if(other != null && !(other.owner instanceof Nature))
            throw new IllegalArgumentException("The chunk "+spawn.getChunk()+" is reserved to "+other.owner);

        CityCreationResult result = mineCity.dataSource.createCity(this);
        storage = result.storage;
        permissionStorage = result.permissionStorage;
        islands = new HashMap<>(1);
        islands.put(result.island.getId(), result.island);
        groups = new HashMap<>(result.groups.size());
        result.groups.forEach(g -> groups.put(g.getIdentityName(), g));

        try
        {
            defaultMessages = mineCity.defaultCityFlags.getDefaultMessages();
            denyAll(mineCity.defaultCityFlags);
        }
        catch(UncheckedDataSourceException e)
        {
            System.err.println("[MineCity][SQL] Exception applying the default city flags!");
            e.getCause().printStackTrace(System.err);
        }

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
    @Slow
    public City(@NotNull MineCity mineCity, @NotNull String identityName, @NotNull String name, @Nullable PlayerID owner,
                @NotNull BlockPos spawn, int id, @NotNull ICityStorage storage,
                @NotNull IExceptPermissionStorage permissionStorage, @Nullable Message defaultDenialMessage,
                @NotNull Tax appliedTax, double investment, double price
    )
            throws DataSourceException
    {
        super(defaultDenialMessage);
        this.price = price;
        this.investment = investment;
        this.appliedTax = appliedTax;
        this.mineCity = mineCity;
        this.name = name;
        this.identityName = identityName;
        this.owner = owner == null? new AdminCity(this) : owner;
        this.spawn = spawn;
        setId(id);
        this.storage = storage;
        this.permissionStorage = permissionStorage;

        Collection<Island> loadedIslands = storage.loadIslands(this);
        this.islands = new HashMap<>();
        loadedIslands.forEach(i-> islands.put(i.getId(), i));

        Collection<Group> loadedGroups = storage.loadGroups(this);
        groups = new HashMap<>(loadedGroups.size());
        loadedGroups.forEach(g -> groups.put(g.getIdentityName(), g));

        defaultMessages = mineCity.defaultCityFlags.getDefaultMessages();
        loadSimplePermissions();
        loadExceptPermissions();
    }

    @Slow
    public synchronized void delete() throws IllegalStateException, DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        try
        {
            List<ChunkPos> chunks = islands.values().stream().map(
                    (DBFunction<Island, IslandArea>) Island::getArea).flatMap(IslandArea::claims)
                    .collect(Collectors.toList());

            storage.deleteCity(this);
            invalid = true;
            groups.values().forEach(Group::checkCityValidity);
            chunks.forEach(mineCity::reloadChunkSlowly);
        }
        catch(UncheckedDataSourceException e)
        {
            throw e.getCause();
        }
    }

    @Slow
    public synchronized Group createGroup(@NotNull String name) throws IllegalArgumentException, DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        String id = identity(name);
        Group conflict = groups.get(id);
        if(conflict != null)
            throw new IllegalArgumentException("The group name '"+name+"' conflicts with '"+conflict.getName()+"'");

        Group group = storage.createGroup(this, id, name);
        groups.put(group.getIdentityName(), group);
        return group;
    }

    @Slow
    public synchronized Group removeGroup(@NotNull String name) throws NoSuchElementException, DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        String id = identity(name);
        Group group = groups.get(id);
        if(group == null)
            throw new NoSuchElementException("Group not found: "+name);

        group.remove();
        groups.remove(id);
        return group;
    }

    public synchronized void removeInvalidGroups()
    {
        groups.values().removeIf(Group::isInvalid);
    }


    public synchronized void updateGroupName(Group group, String oldName)
            throws IllegalStateException, IllegalArgumentException
    {
        String id = group.getIdentityName();
        if(id.equals(oldName))
            throw new IllegalStateException();

        if(!groups.remove(oldName, group))
            throw new IllegalArgumentException();

        groups.put(id, group);
    }

    @Nullable
    public Group getGroup(int id)
    {
        if(invalid)
            return null;

        for(Group group : groups.values())
            if(group.id == id)
                return group;

        return null;
    }

    @Nullable
    public Group getGroup(@NotNull String name)
    {
        if(invalid)
            return null;


        return groups.get(identity(name));
    }

    public Collection<Group> getGroups()
    {
        if(invalid)
            return Collections.emptyList();

        return Collections.unmodifiableCollection(groups.values());
    }

    public Set<String> getGroupNames()
    {
        if(invalid)
            return Collections.emptyNavigableSet();

        return Collections.unmodifiableSet(groups.keySet());
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action)
    {
        if(invalid)
            return Optional.of(mark(INCONSISTENT_CITY_MESSAGE, action));

        if(identity.equals(owner))
            return Optional.empty();

        if(identity.getType() == Identity.Type.NATURE)
            return Optional.of(mark(new Message("Cities are protected from natural actions"), action));

        return super.can(identity, action);
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        if(invalid)
            return Optional.of(mark(INCONSISTENT_CITY_MESSAGE, action));

        if(entity.getIdentity().equals(owner))
            return Optional.empty();

        return super.can(entity, action);
    }

    @Slow
    public void setName(@NotNull String name) throws IllegalArgumentException, DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        String identity = identity(name);
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
        ownerNameCache = null;
        groups.values().forEach(Group::updateCityName);
        plots().forEach(Plot::updateCityName);
    }

    @NotNull
    public String getIdentityName()
    {
        return identityName;
    }

    @Nullable
    public Island getIsland(int id)
    {
        if(invalid)
            return null;

        return islands.get(id);
    }

    @NotNull
    public Collection<Island> islands()
    {
        if(invalid)
            return Collections.emptyList();

        return Collections.unmodifiableCollection(islands.values());
    }

    @NotNull
    @Override
    public OptionalPlayer owner()
    {
        if(invalid)
            return ServerAdmins.INSTANCE;

        return owner;
    }

    public int getSizeX()
    {
        if(invalid)
            return 0;

        return islands.values().stream().mapToInt(Island::getSizeX).sum();
    }

    public int getSizeZ()
    {
        if(invalid)
            return 0;

        return islands.values().stream().mapToInt(Island::getSizeZ).sum();
    }

    public int getChunkCount()
    {
        if(invalid)
            return 0;

        return islands.values().stream().mapToInt(Island::getChunkCount).sum();
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    @NotNull
    public BlockPos getSpawn()
    {
        return spawn;
    }

    @Slow
    public Stream<Island> connectedIslands(@NotNull ChunkPos chunk)
    {
        if(invalid)
            return Stream.empty();

        return Direction.cardinal.stream()
                .map((DBFunction<Direction, Optional<ClaimedChunk>>) d-> mineCity.getOrFetchChunk(chunk.add(d)))
                .filter(Optional::isPresent).map(Optional::get)
                .map(ClaimedChunk::getIsland)
                .filter(Optional::isPresent).map(Optional::get)
                .filter(i-> i.getCity().equals(this))
                ;
    }

    @Slow
    public Stream<Entry<Direction, Island>> connectedIslandsEntries(@NotNull ChunkPos chunk)
    {
        if(invalid)
            return Stream.empty();

        return Direction.cardinal.stream()
                .map((DBFunction<Direction, Entry<Direction, Optional<ClaimedChunk>>>)
                            d-> new SimpleImmutableEntry<>(d, mineCity.getOrFetchChunk(chunk.add(d)))
                )
                .filter(e-> !e.getValue().map(c-> c.reserve).orElse(true))
                .map(e-> (Map.Entry<Direction, Island>) new SimpleImmutableEntry<>(e.getKey(), e.getValue().get().getIsland().orElse(null)))
                .filter(e-> e.getValue() != null)
                .filter(e-> this.equals(e.getValue().getCity()))
                ;
    }

    @Slow
    public Island claim(@NotNull ChunkPos chunk, boolean createIsland)
            throws IllegalArgumentException, DataSourceException, UncheckedDataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        Optional<ClaimedChunk> claimOpt = mineCity.getOrFetchChunk(chunk);
        Optional<City> cityOpt = claimOpt.flatMap(ClaimedChunk::getCityAcceptingReserve);
        if(cityOpt.isPresent() && (cityOpt.get() != this || !claimOpt.get().reserve))
            throw new IllegalArgumentException("The chunk "+chunk+" is reserved");

        Set<Island> islands = connectedIslands(chunk).collect(Collectors.toSet());

        if(islands.isEmpty())
        {
            if(!createIsland)
                throw new IllegalArgumentException("The chunk "+chunk+" is not touching an island owned by city "+identityName);
            Island island = storage.createIsland(this, chunk);
            this.islands.put(island.getId(), island);
            mineCity.reloadChunk(chunk);
            reserveChunks(island);
            return island;
        }
        else if(islands.size() == 1)
        {
            Island island = islands.iterator().next();
            storage.claim(island, chunk);
            //long start = System.currentTimeMillis();
            reserveChunks(island);
            //long end = System.currentTimeMillis();
            //System.out.println("Reserve chunk took "+(end-start)+"ms");
            return island;
        }
        else
        {
            Island mainIsland = storage.claim(islands, chunk);
            islands.stream().filter(island -> !island.equals(mainIsland))
                    .forEach(island -> this.islands.remove(island.getId()));
            reserveChunks(mainIsland);
            return mainIsland;
        }
    }

    @Slow
    public Collection<Island> disclaim(@NotNull ChunkPos chunk, boolean createIslands)
            throws IllegalStateException, IllegalArgumentException, DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        if(islands.size() == 1 && getChunkCount() == 1)
            throw new IllegalStateException("Cannot disclaim the last city's chunk, delete the city instead");

        if(getSpawn().getChunk().equals(chunk))
            throw new IllegalArgumentException("Cannot disclaim the spawn chunk");

        Optional<ClaimedChunk> claim = mineCity.getOrFetchChunk(chunk);
        Island island = claim.flatMap(ClaimedChunk::getIsland).filter(i-> i.getCity().equals(this))
                .orElseThrow(()-> new IllegalArgumentException("The chunk " + chunk + " is not owned by the city " + identityName));

        if(!claim.get().getPlots().isEmpty())
            throw new IllegalArgumentException("Cannot disclaim the chunk "+chunk+" because it contains plots.");

        Map<Direction, Island> islands = connectedIslandsEntries(chunk).filter(e->e.getValue().equals(island))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if(islands.isEmpty())
        {
            storage.deleteIsland(island);
            this.islands.remove(island.getId());
            reserveChunks(island);
            return Collections.singleton(island);
        }
        else if(islands.size() == 1)
        {
            storage.disclaim(chunk, island);
            reserveChunks(island);
            return Collections.singleton(island);
        }
        else
        {
            IslandArea area = mineCity.dataSource.getArea(island);
            area.setClaimed(chunk, false);
            Set<ChunkPos> touching = area.touching(chunk);
            Set<Set<ChunkPos>> groups = touching.stream().map(area::contiguous).collect(Collectors.toSet());

            if(groups.size() == 1)
            {
                storage.disclaim(chunk, island);
                reserveChunks(island);
                return Collections.singletonList(island);
            }

            if(!createIslands)
                throw new IllegalArgumentException("The chunk "+chunk+" is required by other chunks");

            Collection<Island> created = storage.disclaim(chunk, island, groups);
            created.forEach(i-> this.islands.put(i.getId(), i));
            groups.forEach(s-> s.forEach((DisDBConsumer<ChunkPos>) mineCity::reloadChunk));
            Stream.concat(created.stream(), Stream.of(island)).forEach((DisDBConsumer<Island>) this::reserveChunks);
            return created;
        }
    }

    public Optional<Plot> getPlot(String name)
    {
        return islands().stream().map(i-> i.getPlot(name)).filter(Optional::isPresent).map(Optional::get).findAny();
    }

    public Stream<String> plotNames()
    {
        return islands().stream().flatMap(Island::getPlotNames);
    }

    public Stream<String> plotIdNames()
    {
        return islands().stream().flatMap(i-> i.getPlotIdNames().stream());
    }

    public Stream<Plot> plots()
    {
        return islands().stream().flatMap(i-> i.getPlots().stream());
    }

    public Optional<Plot> getPlotAt(BlockPos pos)
    {
        return islands().stream().filter(i-> pos.world.equals(i.world)).map(i-> i.getPlotAt(pos))
                .filter(Optional::isPresent).map(Optional::get).findAny();
    }

    public Stream<Plot> getPlotsAt(ChunkPos pos)
    {
        return islands().stream().filter(i-> pos.world.equals(i.world)).flatMap(i-> i.getPlotsAt(pos));
    }

    @Slow
    protected void reserveChunks(Island island) throws DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        IslandArea area = mineCity.dataSource.getArea(island);
        int rangeX = island.getSizeX()/2;
        int rangeZ = island.getSizeZ()/2;

        IslandArea reserve;
        if(rangeX == 0 && rangeZ == 0)
            reserve = area;
        else
        {
            reserve = new IslandArea(island, area.x - rangeX, area.z - rangeZ,
                    new boolean[area.claims.length + rangeX*2][area.claims[0].length + rangeZ*2]
            );

            area.claims().forEach(p-> {
                reserve.claims[p.x-reserve.x][p.z-reserve.z] = true;
                for(int rx=-rangeX; rx <= rangeX; rx++)
                    for(int rz=-rangeZ; rz <= rangeZ; rz++)
                        reserve.claims[p.x+rx-reserve.x][p.z+rz-reserve.z] = true;
            });
        }

        //long start = System.currentTimeMillis();
        Collection<ChunkPos> update = storage.reserve(reserve);
        //long end = System.currentTimeMillis();
        //System.out.println("SQL Call took "+(end-start)+"ms");
        update.forEach(mineCity::reloadChunkSlowly);
    }

    @Slow
    public void setSpawn(@NotNull BlockPos pos) throws DataSourceException,IllegalArgumentException
    {
        if(invalid)
            throw new IllegalStateException();

        if(!mineCity.getOrFetchChunk(pos.getChunk()).map(c->c.owner).filter(o->o instanceof Island).map(o->(Island)o)
                .filter(i-> i.getCity().equals(this)).isPresent() )
            throw new IllegalArgumentException("The block "+pos+" is not part of the city");

        storage.setSpawn(this, pos);
        this.spawn = pos;
    }

    /**
     * Changes the owner of the city and saves it immediately
     * @param owner The new owner
     * @throws DataSourceException If the city is registered and the change failed. The owner will not be set in this case.
     */
    @Slow
    public void setOwner(@NotNull OptionalPlayer owner) throws DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        storage.setOwner(this, owner);
        this.owner = owner.getType() == Identity.Type.ADMINS? new AdminCity(this) : owner;
        ownerNameCache = null;
        plots().forEach(Plot::updateCityName);
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
            throw new IllegalStateException("Tried to change the city's \""+identityName+"\" ID from "+this.id+" to "+id);

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

        if(owner.getType() == Identity.Type.ADMINS)
            return LegacyFormat.RED;

        return LegacyFormat.CITY_COLORS[id%LegacyFormat.CITY_COLORS.length];
    }

    public boolean isInvalid()
    {
        return invalid;
    }

    @Override
    public String toString()
    {
        return "City{" +
                "id=" + id +
                ", identityName='" + identityName + '\'' +
                "}";
    }

    @Override
    public Message ownerName()
    {
        Message cache = this.ownerNameCache;
        if(cache != null && --ownerNameLife > 0)
            return this.ownerNameCache;

        ownerNameLife = 127;
        Message msg;
        if(owner.getType() == Identity.Type.ADMINS)
        {
            msg = new Message("action.denied.city.admin", "${name}", new Object[]{"name", name});
        }
        else
        {
            msg = new Message("action.denied.city.normal", "${name} ~ ${owner}", new Object[][]{
                    {"name", name}, {"owner", owner.getName()}
            });
        }

        return this.ownerNameCache = msg;
    }

    @NotNull
    public Tax getAppliedTax()
    {
        return appliedTax;
    }

    public double getInvestment()
    {
        return investment;
    }

    @Slow
    public synchronized void invested(double value) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException("This instance is no longer valid");

        investment = storage.invested(this, value);
    }

    public double getPrice()
    {
        return price;
    }

    @Slow
    public void setPrice(double price) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException("This instance is no longer valid");

        storage.setPrice(this, price);
        this.price = price;
    }
}
