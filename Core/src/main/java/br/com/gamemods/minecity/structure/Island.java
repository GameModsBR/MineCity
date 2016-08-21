package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.IExceptPermissionStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class Island implements ChunkOwner
{
    public final Reserve reserve = new Reserve(this);

    @NotNull
    protected final ICityStorage storage;

    @NotNull
    protected final IExceptPermissionStorage permissionStorage;
    public final int id;
    public final WorldDim world;
    protected Map<String, Plot> plots;

    public Island(@NotNull ICityStorage storage, @NotNull IExceptPermissionStorage permissionStorage,
                  int id, WorldDim world, Set<Plot> plots)
    {
        this.storage = storage;
        this.permissionStorage = permissionStorage;
        this.id = id;
        this.world = world;
        this.plots = new HashMap<>(plots.size());
        plots.forEach(plot -> this.plots.put(plot.getIdentityName(), plot));
    }

    public Island(@NotNull ICityStorage storage, @NotNull IExceptPermissionStorage permissionStorage,
                  int id, WorldDim world)
            throws DataSourceException
    {
        this.storage = storage;
        this.permissionStorage = permissionStorage;
        this.id = id;
        this.world = world;

        Set<Plot> plots = storage.loadPlots(this);
        this.plots = new HashMap<>(plots.size());
        plots.forEach(plot -> this.plots.put(plot.getIdentityName(), plot));
    }

    @Slow
    public Plot createPlot(@NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn, @NotNull Shape shape)
            throws DataSourceException
    {
        String identity = StringUtil.identity(name);
        for(Island island : getCity().islands())
        {
            Plot conflict = island.plots.get(identity);
            if(conflict != null)
                throw new IllegalArgumentException("The name "+name+" conflicts with "+conflict.getName());
        }

        if(!spawn.world.equals(world))
            throw new IllegalArgumentException("The spawn is in a different world");

        Plot plot = new Plot(storage, permissionStorage, this, identity, name, owner, spawn, shape);
        plots.put(identity, plot);
        return plot;
    }

    public Optional<Plot> getPlot(String name)
    {
        return Optional.ofNullable(plots.get(StringUtil.identity(name)));
    }

    public Collection<Plot> getPlots()
    {
        return Collections.unmodifiableCollection(plots.values());
    }

    public Stream<Plot> getPlotsAt(ChunkPos pos)
    {
        if(!pos.world.equals(world))
            return Stream.empty();

        return plots.values().stream().filter(plot -> plot.getShape().affects(pos));
    }

    public Optional<Plot> getPlotAt(BlockPos pos)
    {
        if(!pos.world.equals(world))
            return Optional.empty();

        for(Plot plot : plots.values())
            if(plot.getShape().contains(pos.x, pos.y, pos.z))
                return Optional.of(plot);

        return Optional.empty();
    }

    public Stream<String> getPlotNames()
    {
        return plots.values().stream().map(Plot::getName);
    }

    public Set<String> getPlotIdNames()
    {
        return Collections.unmodifiableSet(plots.keySet());
    }

    public final int getId()
    {
        return id;
    }

    @NotNull
    public final WorldDim getWorld()
    {
        return world;
    }

    @NotNull
    public abstract City getCity();
    public abstract int getSizeX();
    public abstract int getSizeZ();
    public abstract int getChunkCount();

    public IslandArea getArea() throws DataSourceException
    {
        return getCity().mineCity.dataSource.getArea(this);
    }

    @Override
    public String toString()
    {
        return "Island{" +
                "id=" + id +
                ", world=" + world +
                ", city=" + getCity().getName() +
                '}';
    }
}
