package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Inconsistency implements ChunkOwner
{
    public static final Inconsistency INSTANCE = new Inconsistency();
    public static final WorldDim WORLD = new WorldDim(-10000, "inconsistency", "Inconsistent");
    private static MineCity mineCity;
    private static Island island;
    private static City city;
    private Inconsistency(){}

    public static void setMineCity(MineCity mineCity)
    {
        Inconsistency.mineCity = mineCity;
    }

    public static City getInconsistentCity(MineCity mineCity)
    {
        if(city == null)
            city = new City(mineCity, "#inconsistent", "#Inconsistency", null, new BlockPos(WORLD, 0,0,0),
                    Collections.singleton(island = new InconsistentIsland()), Integer.MAX_VALUE, new VoidStorage());
        return city;
    }

    public static City getInconsistentCity()
    {
        return getInconsistentCity(mineCity);
    }

    public static Island getInconsistentIsland(MineCity mineCity)
    {
        if(island == null)
            getInconsistentCity(mineCity);
        return island;
    }

    public static Island getInconsistentIsland()
    {
        return getInconsistentIsland(mineCity);
    }

    private static class InconsistentIsland implements Island
    {
        @Override
        public int getId()
        {
            return -1;
        }

        @NotNull
        @Override
        public WorldDim getWorld()
        {
            return WORLD;
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
            return 0;
        }

        @Override
        public int getSizeZ()
        {
            return 0;
        }

        @Override
        public int getChunkCount()
        {
            return 0;
        }
    }

    private static class VoidStorage implements ICityStorage
    {
        @Override
        public void setOwner(@NotNull City city, @Nullable PlayerID owner)
                throws DataSourceException, IllegalStateException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setSpawn(@NotNull City city, @NotNull BlockPos spawn)
                throws DataSourceException, IllegalStateException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Island createIsland(@NotNull City city, @NotNull ChunkPos chunk)
                throws DataSourceException, IllegalStateException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void claim(@NotNull Island island, @NotNull ChunkPos chunk)
                throws DataSourceException, IllegalStateException, ClassCastException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Island claim(@NotNull Set<Island> islands, @NotNull ChunkPos chunk)
                throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void deleteIsland(@NotNull Island island)
                throws DataSourceException, IllegalArgumentException, ClassCastException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void disclaim(@NotNull ChunkPos chunk, @NotNull Island island)
                throws DataSourceException, IllegalArgumentException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Collection<Island> disclaim(@NotNull ChunkPos chunk, @NotNull Island island,
                                           @NotNull Set<Set<ChunkPos>> groups)
                throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException, IllegalArgumentException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public IslandArea getArea(@NotNull Island island)
                throws DataSourceException, ClassCastException, IllegalArgumentException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setName(@NotNull City city, @NotNull String identity, @NotNull String name)
                throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }
    }
}
