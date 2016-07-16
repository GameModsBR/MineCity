package br.com.gamemods.minecity.test.datasource;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.PlayerID;
import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.api.ChunkPos;
import br.com.gamemods.minecity.api.WorldDim;
import br.com.gamemods.minecity.datasource.CityCreationResult;
import br.com.gamemods.minecity.datasource.DataSourceException;
import br.com.gamemods.minecity.datasource.ICityStorage;
import br.com.gamemods.minecity.datasource.IDataSource;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeDataSource implements IDataSource, ICityStorage
{
    public MineCity mineCity;
    private Map<ChunkPos, Island> claims = new HashMap<>();
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
        FakeIsland island = new FakeIsland(spawn.world, city);
        cities.put(cityId, city);
        claims.put(spawnChunk, island);

        try
        {
            ClaimedChunk chunk = mineCity.getChunk(spawnChunk);
            if(chunk != null)
                mineCity.loadChunk(spawnChunk);
        }
        catch(DataSourceException e)
        {
            System.err.println("[MineCity][DS] Exception reloading a chunk");
            e.printStackTrace(System.err);
        }

        return new CityCreationResult(this, island);
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
        int sizeX = 1, sizeZ = 1, chunkCount = 1;

        public FakeIsland(WorldDim world, City city)
        {
            this.world = world;
            this.city = city;
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
            return sizeX;
        }

        @Override
        public int getSizeZ()
        {
            return sizeZ;
        }

        @Override
        public int getChunkCount()
        {
            return chunkCount;
        }
    }
}
