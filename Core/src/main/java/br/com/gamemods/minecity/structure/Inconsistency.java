package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.IExceptPermissionStorage;
import br.com.gamemods.minecity.datasource.api.INatureStorage;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Inconsistency implements ChunkOwner
{
    public static final Message INCONSISTENT_CHUNK_MESSAGE = new Message("inconsistent.chunk", "This chunk is inconsistent.");
    public static final Inconsistency INSTANCE = new Inconsistency();
    public static final WorldDim WORLD = new WorldDim(-10000, "inconsistency", "Inconsistent");
    private static MineCity mineCity;
    private static Island island;
    private static City city;
    private Inconsistency(){}

    public static Nature nature(WorldDim world)
    {
        VoidStorage voidStorage = new VoidStorage();
        try
        {
            return new Nature(mineCity, world, INCONSISTENT_CHUNK_MESSAGE, voidStorage, voidStorage, true);
        }
        catch(DataSourceException unexpected)
        {
            throw new RuntimeException(unexpected);
        }
    }

    public static void setMineCity(MineCity mineCity)
    {
        Inconsistency.mineCity = mineCity;
    }

    public static City getInconsistentCity(MineCity mineCity)
    {
        if(city == null)
        {
            try
            {
                VoidStorage voidStorage = new VoidStorage();
                synchronized(WORLD)
                {
                    city = new City(mineCity, "#inconsistent", "#Inconsistency", null, new BlockPos(WORLD, 0, 0, 0),
                            Collections.singleton(island = new InconsistentIsland(voidStorage)), -1000, voidStorage,
                            voidStorage, null
                    );
                }
            }
            catch(DataSourceException unexpected)
            {
                throw new RuntimeException(unexpected);
            }
            Arrays.asList(PermissionFlag.values()).forEach(f-> city.deny(f, INCONSISTENT_CHUNK_MESSAGE));
            city.allow(PermissionFlag.LEAVE);
        }
        return city;
    }

    public static ClaimedChunk claim(ChunkPos pos)
    {
        Nature nature = pos.world.nature;
        if(nature != null)
            getInconsistentCity(nature.mineCity);
        else
            getInconsistentCity();

        ClaimedChunk chunk = new ClaimedChunk(INSTANCE, pos);
        chunk.invalidate();
        return chunk;
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

    private static class InconsistentIsland extends Island
    {
        public InconsistentIsland(VoidStorage storage)
        {
            super(storage, storage, -1, WORLD, Collections.emptySet());
        }

        @NotNull
        @Override
        public City getCity()
        {
            City c = city;
            if(c == null)
                synchronized(WORLD)
                {
                    return city;
                }

            return c;
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

    private static class VoidStorage implements ICityStorage, IExceptPermissionStorage, INatureStorage
    {
        @Override
        public void setOwner(@NotNull City city, @NotNull OptionalPlayer owner)
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
        public void deleteCity(@NotNull City city) throws DataSourceException
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

        @Override
        public void setName(@NotNull City city, @NotNull String identity, @NotNull String name)
                throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setName(@NotNull Group group, @NotNull String identity, @NotNull String name)
                throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void addMember(@NotNull Group group, @NotNull Identity<?> member)
                throws DataSourceException, UnsupportedOperationException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void removeMember(@NotNull Group group, @NotNull Identity<?> member)
                throws DataSourceException, UnsupportedOperationException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void addManager(@NotNull Group group, @NotNull PlayerID manager) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void removeManager(@NotNull Group group, @NotNull PlayerID manager) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void deleteGroup(@NotNull Group group) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Collection<ChunkPos> reserve(@NotNull IslandArea reserve) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Group createGroup(@NotNull City city, @NotNull String id, @NotNull String name)
                throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Collection<Group> loadGroups(@NotNull City city) throws DataSourceException
        {
            if(city.getId() != -1000)
                throw new DataSourceException("Inconsistent city!");
            return Collections.emptyList();
        }

        @Override
        public void setDefaultMessage(@NotNull SimpleFlagHolder holder, @Nullable Message message)
                throws DataSourceException
        {
            if(holder != city)
                throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void deny(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag, @Nullable Message message)
                throws DataSourceException
        {
            if(holder != city)
                throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void denyAll(SimpleFlagHolder holder, Map<? extends PermissionFlag, ? extends Message> flags)
                throws DataSourceException
        {
            if(holder != city)
                throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void allow(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag) throws DataSourceException
        {
            if(holder != city)
                throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void allowAll(@NotNull SimpleFlagHolder holder) throws DataSourceException
        {
            if(holder != city)
                throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public EnumMap<PermissionFlag, Message> loadSimplePermissions(@NotNull SimpleFlagHolder holder)
                throws DataSourceException
        {
            EnumMap<PermissionFlag, Message> result = new EnumMap<>(PermissionFlag.class);
            Arrays.stream(PermissionFlag.values()).forEach(f-> result.put(f, INCONSISTENT_CHUNK_MESSAGE));
            return result;
        }

        @Override
        public void set(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, boolean allow,
                        @NotNull Identity<?> identity, @Nullable Message message) throws DataSourceException
        {
            if(holder != city)
                throw new UncheckedDataSourceException(new DataSourceException("Inconsistent city!"));
        }

        @Override
        public void remove(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag,
                           @NotNull Identity<?> identity)
                throws DataSourceException
        {
            if(holder != city)
                throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Map<PermissionFlag, Map<Identity<?>, Optional<Message>>> loadExceptPermissions(@NotNull ExceptFlagHolder holder)
                throws DataSourceException
        {
            return Collections.emptyMap();
        }

        @Override
        public int createPlot(Plot plot) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setOwner(@NotNull Plot plot, @Nullable PlayerID owner)
                throws DataSourceException, IllegalStateException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setShape(@NotNull Plot plot, @NotNull Shape shape, BlockPos spawn, @NotNull Island newIsland) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setName(@NotNull Plot plot, @NotNull String identity, @NotNull String name)
                throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setSpawn(@NotNull Plot plot, @NotNull BlockPos spawn) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void deletePlot(@NotNull Plot plot) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @NotNull
        @Override
        public Set<Plot> loadPlots(@NotNull Island island) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent city!");
        }

        @Override
        public void setCityCreationDenied(@NotNull Nature nature, boolean denied) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent nature!");
        }

        @Override
        public void setName(@NotNull Nature nature, @NotNull String name) throws DataSourceException
        {
            throw new DataSourceException("Inconsistent nature!");
        }
    }

    @Override
    public String toString()
    {
        return "#Inconsistency!";
    }
}
