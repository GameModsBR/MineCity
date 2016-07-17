package br.com.gamemods.minecity.datasource.test;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.CityCreationResult;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.IDataSource;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FakeDataSource implements IDataSource, ICityStorage
{
    public MineCity mineCity;
    private Map<ChunkPos, FakeIsland> claims = new HashMap<>();
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

        return new CityCreationResult(this, island);
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

        Stream<FakeIsland> sqlIslands = islands.stream().map(island -> (FakeIsland) island);
        FakeIsland mainIsland = sqlIslands.max((a, b) -> a.getChunkCount() - b.getChunkCount()).get();
        List<FakeIsland> merge = sqlIslands.filter(island -> island != mainIsland).collect(Collectors.toList());

        List<ChunkPos> chunksToUpdate = claims.entrySet().stream()
            .filter(e-> merge.contains(e.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());

        chunksToUpdate.stream().forEach(pos-> claims.put(pos, mainIsland));
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

        chunksToUpdate.stream().forEach((DBConsumer<ChunkPos>) pos-> mineCity.reloadChunk(pos));
        return mainIsland;
    }



    @Override
    public void setOwner(@NotNull City city, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException
    {

    }

    @Override
    public void setSpawn(@NotNull City city, @NotNull BlockPos spawn) throws DataSourceException, IllegalStateException
    {

    }

    private class FakeIsland implements Island
    {
        int id = nextIslandId.getAndIncrement();
        WorldDim world;
        City city;
        int minX, maxX, minZ, maxZ, chunkCount = 1;

        public FakeIsland(WorldDim world, City city, ChunkPos chunk)
        {
            this.world = world;
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

        @Override
        public int getId()
        {
            return id;
        }

        @NotNull
        @Override
        public WorldDim getWorld()
        {
            return world;
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
            return maxX - minX + 1;
        }

        @Override
        public int getSizeZ()
        {
            return maxZ - minZ + 1;
        }

        @Override
        public int getChunkCount()
        {
            return chunkCount;
        }
    }
}
