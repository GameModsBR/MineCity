package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class Plot
{
    public final int id;

    @NotNull
    private final ICityStorage storage;

    @NotNull
    private Island island;

    @NotNull
    private String identityName;

    @NotNull
    private String name;

    @Nullable
    private PlayerID owner;

    @NotNull
    private BlockPos spawn;

    @NotNull
    private Shape shape;

    private boolean invalid;

    public Plot(@NotNull ICityStorage storage, int id, @NotNull Island island, @NotNull String identityName,
                @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn, @NotNull Shape shape)
    {
        this.storage = storage;
        this.island = island;
        this.identityName = identityName;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        this.shape = shape;
        this.id = id;
    }

    @Slow
    public Plot(@NotNull ICityStorage storage, @NotNull Island island, @NotNull String identityName,
                @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn, @NotNull Shape shape)
            throws DataSourceException
    {
        this.storage = storage;
        this.island = island;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        this.shape = shape;
        this.identityName = identityName;
        this.id = storage.createPlot(this);

        MineCity mineCity = island.getCity().mineCity;
        shape.chunks(island.world).forEach(mineCity::reloadChunkSlowly);
    }

    @Slow
    public synchronized void delete() throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        storage.deletePlot(this);
        invalid = true;
        island.plots.remove(identityName, this);

        MineCity mineCity = island.getCity().mineCity;
        shape.chunks(island.world).forEach(mineCity::reloadChunkSlowly);
    }

    @Slow
    public void setOwner(@Nullable PlayerID owner)
            throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        if(Objects.equals(owner, this.owner))
            return;

        storage.setOwner(this, owner);
        this.owner = owner;
    }

    @Slow
    public void setShape(@NotNull Shape shape) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        storage.setShape(this, shape);
        this.shape = shape;
    }

    @Slow
    public void setName(@NotNull String name) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        if(name.equals(this.name))
            return;

        String identity = StringUtil.identity(name);
        for(Island island: this.island.getCity().islands())
        {
            Plot conflict = island.plots.get(identity);
            if(conflict != null)
                throw new IllegalArgumentException("The name "+name+" conflicts with "+conflict.getName());
        }

        String old = identityName;
        storage.setName(this, identity, name);
        identityName = identity;
        this.name = name;
        if(island.plots.remove(old, this))
            island.plots.put(identity, this);
    }

    @Slow
    public void setSpawn(@NotNull BlockPos pos) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        if(pos.equals(spawn))
            return;

        if(!pos.world.equals(spawn.world))
            throw new IllegalArgumentException();

        storage.setSpawn(this, pos);
        this.spawn = pos;
    }

    @NotNull
    public Shape getShape()
    {
        return shape;
    }

    @NotNull
    public Island getIsland()
    {
        return island;
    }

    @NotNull
    public String getIdentityName()
    {
        return identityName;
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    /**
     * The direct owner of the plot, a plot without direct owner is owned by the city's mayor
     * @return Empty if the plot is owned by the city's mayor
     */
    @NotNull
    public Optional<PlayerID> getOwner()
    {
        return Optional.ofNullable(owner);
    }

    /**
     * The actual owner of the plot
     * @return {@code null} if the plot is owned by the server administrators
     */
    @Nullable
    public PlayerID owner()
    {
        return owner != null? owner : island.getCity().getOwner();
    }

    @NotNull
    public BlockPos getSpawn()
    {
        return spawn;
    }

    @NotNull
    public City getCity()
    {
        return island.getCity();
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Plot plot = (Plot) o;

        return id == plot.id;

    }

    @Override
    public int hashCode()
    {
        return id;
    }
}
