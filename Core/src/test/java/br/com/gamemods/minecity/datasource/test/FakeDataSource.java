package br.com.gamemods.minecity.datasource.test;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.*;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.structure.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FakeDataSource implements IDataSource, ICityStorage, IExceptPermissionStorage, INatureStorage
{
    public MineCity mineCity;
    private Map<ChunkPos, FakeIsland> claims = new HashMap<>();
    private Map<ChunkPos, FakeIsland> reserves = new HashMap<>();
    private Map<Integer, City> cities = new HashMap<>();
    private AtomicInteger nextCityId = new AtomicInteger(1), nextIslandId = new AtomicInteger(1);

    @Nullable
    @Override
    public ClaimedChunk getCityChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        Island island = claims.get(pos);
        if(island == null)
            return null;

        return new ClaimedChunk(island, pos);
    }

    @NotNull
    @Override
    public CityCreationResult createCity(@NotNull City city) throws DataSourceException, IllegalStateException
    {
        if(city.getId() > 0)
            throw new IllegalStateException();

        BlockPos spawn = city.getSpawn();
        ChunkPos spawnChunk = spawn.getChunk();
        ClaimedChunk claim = getCityChunk(spawnChunk);
        if(claim != null)
            throw new IllegalStateException("The chunk "+ spawnChunk +" is already claimed: "+claim);

        int cityId = nextCityId.getAndIncrement();
        city.setId(cityId);
        FakeIsland island = new FakeIsland(spawn.world, city, spawnChunk);
        cities.put(cityId, city);
        claims.put(spawnChunk, island);

        try
        {
            mineCity.reloadChunk(spawnChunk);
        }
        catch(DataSourceException e)
        {
            System.err.println("[MineCity][DS] Exception reloading a chunk");
            e.printStackTrace(System.err);
        }

        return new CityCreationResult(this, this, island, Collections.emptyList());
    }

    @Override
    public void deleteCity(@NotNull City city) throws DataSourceException
    {
        cities.remove(city.getId());
    }

    @Nullable
    @Override
    public String checkNameConflict(@NotNull String identityName)
    {
        identityName = StringUtil.identity(identityName);
        for(City city : cities.values())
            if(city.getIdentityName().equals(identityName))
                return city.getName();

        return null;
    }

    @NotNull
    @Override
    public Island createIsland(@NotNull City city, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException
    {
        if(claims.containsKey(chunk))
            throw new DataSourceException();

        FakeIsland fakeIsland = new FakeIsland(chunk.world, city, chunk);
        claims.put(chunk, fakeIsland);
        mineCity.reloadChunk(chunk);
        return fakeIsland;
    }

    @Override
    public void claim(@NotNull Island island, @NotNull ChunkPos chunk) throws DataSourceException, IllegalStateException
    {
        if(claims.containsKey(chunk))
            throw new DataSourceException();

        FakeIsland fakeIsland = (FakeIsland) island;
        claims.put(chunk, fakeIsland);
        fakeIsland.add(chunk);
        mineCity.reloadChunk(chunk);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @NotNull
    @Override
    public Island claim(@NotNull Set<Island> islands, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException, NoSuchElementException
    {
        if(islands.size() == 1)
        {
            Island island = islands.iterator().next();
            claim(island, chunk);
            return island;
        }

        List<FakeIsland> sqlIslands = islands.stream().map(island -> (FakeIsland) island).collect(Collectors.toList());
        FakeIsland mainIsland = sqlIslands.stream().max((a, b) -> a.getChunkCount() - b.getChunkCount()).get();
        List<FakeIsland> merge = sqlIslands.stream().filter(island -> island != mainIsland).collect(Collectors.toList());

        List<ChunkPos> chunksToUpdate = claims.entrySet().stream()
            .filter(e-> merge.contains(e.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());

        chunksToUpdate.forEach(pos -> claims.put(pos, mainIsland));
        claims.put(chunk, mainIsland);
        mainIsland.add(chunk);

        merge.forEach(island -> {
            mainIsland.minX = Math.min(mainIsland.minX, island.minX);
            mainIsland.maxX = Math.max(mainIsland.maxX, island.maxX);
            mainIsland.minZ = Math.min(mainIsland.minZ, island.minZ);
            mainIsland.maxZ = Math.max(mainIsland.maxZ, island.maxZ);
            mainIsland.chunkCount += island.chunkCount;

            island.minX = island.maxX = island.minZ = island.maxZ = island.chunkCount = 0;
        });

        chunksToUpdate.forEach((DBConsumer<ChunkPos>) pos -> mineCity.reloadChunk(pos));
        return mainIsland;
    }



    @Override
    public void setOwner(@NotNull City city, @NotNull OptionalPlayer owner) throws DataSourceException, IllegalStateException
    {
        // Nothing needs to be done here
    }

    @Override
    public void setSpawn(@NotNull City city, @NotNull BlockPos spawn) throws DataSourceException, IllegalStateException
    {
        // Nothing needs to be done here
    }

    @Override
    public void deleteIsland(@NotNull Island island)
            throws DataSourceException, IllegalArgumentException, ClassCastException
    {
        FakeIsland fakeIsland = (FakeIsland) island;
        if(fakeIsland.chunkCount == 0) throw new IllegalArgumentException();


        Collection<FakeIsland> values = claims.values();
        //noinspection StatementWithEmptyBody
        while(values.remove(fakeIsland)){}

        fakeIsland.chunkCount = fakeIsland.maxX = fakeIsland.minX = fakeIsland.maxZ = fakeIsland.minZ = 0;
        mineCity.reloadChunks(c-> c.getIsland().filter(island::equals).isPresent());
    }

    private void updateCount(FakeIsland fakeIsland)
    {
        List<ChunkPos> claims = this.claims.entrySet().stream().filter(e -> e.getValue().equals(fakeIsland))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for(ChunkPos claim : claims)
        {
            minX = Math.min(minX, claim.x);
            maxX = Math.max(maxX, claim.x);
            minZ = Math.min(minZ, claim.z);
            maxZ = Math.max(maxZ, claim.z);
        }

        fakeIsland.minX = minX;
        fakeIsland.maxX = maxX;
        fakeIsland.minZ = minZ;
        fakeIsland.maxZ = maxZ;
        fakeIsland.chunkCount = claims.size();
    }

    @Override
    public void disclaim(@NotNull ChunkPos chunk, @NotNull Island island)
            throws DataSourceException, IllegalArgumentException
    {
        FakeIsland fakeIsland = (FakeIsland) island;
        if(fakeIsland.chunkCount == 0) throw new IllegalArgumentException();

        if(claims.remove(chunk) == null)
            throw new DataSourceException();

        updateCount(fakeIsland);

        try
        {
            mineCity.reloadChunk(chunk);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @NotNull
    @Override
    public Collection<Island> disclaim(@NotNull ChunkPos chunk, @NotNull Island island,
                                       @NotNull Set<Set<ChunkPos>> groups)
            throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException, IllegalArgumentException
    {
        FakeIsland fakeIsland = (FakeIsland) island;
        if(fakeIsland.chunkCount == 0) throw new IllegalArgumentException();

        Set<ChunkPos> mainGroup = groups.stream().max((a,b)-> a.size()-b.size() ).get();
        groups = groups.stream().filter(s-> s != mainGroup).collect(Collectors.toSet());

        claims.remove(chunk);

        List<Island> islands = new ArrayList<>();
        for(Set<ChunkPos> group : groups)
        {
            Iterator<ChunkPos> iter = group.iterator();
            ChunkPos first = iter.next();
            FakeIsland groupIsland = new FakeIsland(fakeIsland.world, fakeIsland.city, first);
            this.claims.put(first, groupIsland);

            while(iter.hasNext())
            {
                ChunkPos pos = iter.next();
                this.claims.put(pos, groupIsland);
                groupIsland.add(pos);
            }

            islands.add(groupIsland);
        }

        updateCount(fakeIsland);

        return islands;
    }

    @NotNull
    @Override
    public IslandArea getArea(@NotNull Island island)
            throws DataSourceException, ClassCastException, IllegalArgumentException
    {
        FakeIsland fakeIsland = (FakeIsland) island;

        return new IslandArea(fakeIsland, claims.entrySet().stream().filter(e-> e.getValue().equals(fakeIsland))
                .map(Map.Entry::getKey).collect(Collectors.toList())
        );
    }

    @NotNull
    @Override
    public Collection<ChunkPos> reserve(@NotNull IslandArea reserve)
    {
        reserve.claims().forEachOrdered(c-> reserves.putIfAbsent(c, (FakeIsland)reserve.island));
        return reserve.claims().collect(Collectors.toSet());
    }

    @Override
    public void setName(@NotNull City city, @NotNull String identity, @NotNull String name) throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void setName(@NotNull Group group, @NotNull String identity, @NotNull String name) throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void addMember(@NotNull Group group, @NotNull Identity<?> member)
            throws DataSourceException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeMember(@NotNull Group group, @NotNull Identity<?> member)
            throws DataSourceException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addManager(@NotNull Group group, @NotNull PlayerID manager) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeManager(@NotNull Group group, @NotNull PlayerID manager) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteGroup(@NotNull Group group) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Collection<Group> loadGroups(@NotNull City city) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Group createGroup(@NotNull City city, @NotNull String id, @NotNull String name) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Optional<Set<String>> getGroupNames(@NotNull String cityName)
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Map<String, Set<String>> getGroups()
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<GroupID> getEntityGroups(Identity<?> identity) throws DataSourceException
    {
        return cities.values().stream().map(City::getGroups).flatMap(Collection::stream)
                .filter(g-> g.isMember(identity)).map(Group::getIdentity).collect(Collectors.toSet());
    }

    @Override
    public void initDB() throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void close() throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @NotNull
    @Override
    public Optional<City> getCityByName(@NotNull String name)
    {
        name = StringUtil.identity(name);
        for(City city : cities.values())
            if(city.getIdentityName().equals(name))
                return Optional.of(city);
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<PlayerID> getPlayer(@NotNull String name) throws DataSourceException
    {
        for(City city : cities.values())
        {
            PlayerID owner = city.owner().player();
            if(owner != null && owner.getName().equals(name))
                return Optional.of(owner);
        }

        return Optional.empty();
    }

    @NotNull
    @Override
    public Supplier<Stream<String>> cityNameSupplier()
    {
        return ()-> cities.values().stream().map(City::getName);
    }

    @Override
    public void setDefaultMessage(@NotNull SimpleFlagHolder holder, @Nullable Message message)
    {
        // Nothing needs to be done here
    }

    @Override
    public void deny(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag, @Nullable Message message)
            throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void denyAll(SimpleFlagHolder holder, Map<? extends PermissionFlag, ? extends Message> flags)
            throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void allow(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag) throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void allowAll(@NotNull SimpleFlagHolder holder) throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void set(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, boolean allow,
                    @NotNull Identity<?> identity, @Nullable Message message) throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public void remove(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, @NotNull Identity<?> identity)
            throws DataSourceException
    {
        // Nothing needs to be done here
    }

    @Override
    public int createPlot(Plot plot) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwner(@NotNull Plot plot, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShape(@NotNull Plot plot, @NotNull Shape shape, BlockPos spawn, @NotNull Island newIsland) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(@NotNull Plot plot, @NotNull String identity, @NotNull String name) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSpawn(@NotNull Plot plot, @NotNull BlockPos spawn) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deletePlot(@NotNull Plot plot) throws DataSourceException
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<Plot> loadPlots(@NotNull Island island) throws DataSourceException
    {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public Map<PermissionFlag, Map<Identity<?>, Optional<Message>>> loadExceptPermissions(@NotNull ExceptFlagHolder holder)
            throws DataSourceException
    {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public EnumMap<PermissionFlag, Message> loadSimplePermissions(@NotNull SimpleFlagHolder holder)
            throws DataSourceException
    {
        return new EnumMap<>(PermissionFlag.class);
    }

    @NotNull
    @Override
    public Nature getNature(@NotNull WorldDim world) throws DataSourceException
    {
        return new Nature(mineCity, world, this, this);
    }

    @Override
    public void setCityCreationDenied(@NotNull Nature nature, boolean denied) throws DataSourceException
    {
        // Nothing needs to be done
    }

    @Override
    public void setName(@NotNull Nature nature, @NotNull String name) throws DataSourceException
    {
        // Nothing needs to be done
    }

    @Override
    public int getCityCount(PlayerID playerId)
    {
        //TODO Implement
        return 0;
    }

    private class FakeIsland extends Island
    {
        City city;
        int minX, maxX, minZ, maxZ, chunkCount = 1;

        public FakeIsland(WorldDim world, City city, ChunkPos chunk)
        {
            super(FakeDataSource.this, FakeDataSource.this, nextIslandId.getAndIncrement(), world, Collections.emptySet());
            this.city = city;
            minX = maxX = chunk.x;
            minZ = maxZ = chunk.z;
        }

        void add(ChunkPos chunk)
        {
            minX = Math.min(minX, chunk.x);
            maxX = Math.max(maxX, chunk.x);
            minZ = Math.min(minZ, chunk.z);
            maxZ = Math.max(maxZ, chunk.z);
            chunkCount++;
        }

        @NotNull
        @Override
        public City getCity()
        {
            return city;
        }

        @Override
        public int getSizeX()
        {
            return chunkCount == 0? 0 : maxX - minX + 1;
        }

        @Override
        public int getSizeZ()
        {
            return chunkCount == 0? 0 : maxZ - minZ + 1;
        }

        @Override
        public int getChunkCount()
        {
            return chunkCount;
        }
    }
}
