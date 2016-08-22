package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.AdminPlot;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.OptionalPlayer;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ExceptStoredHolder;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.IExceptPermissionStorage;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

public final class Plot extends ExceptStoredHolder
{
    public static final Message INCONSISTENT_PLOT_MESSAGE = new Message("inconsistent.plot", "This plot is inconsistent.");

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

    private AdminPlot adminOwner;

    public Plot(@NotNull MineCity mineCity, @NotNull ICityStorage storage, @NotNull IExceptPermissionStorage permissionStorage, int id,
                @NotNull Island island, @NotNull String identityName, @NotNull String name, @Nullable PlayerID owner,
                @NotNull BlockPos spawn, @NotNull Shape shape, @Nullable Message defaultMessage)
            throws DataSourceException
    {
        super(defaultMessage);
        this.permissionStorage = permissionStorage;
        this.storage = storage;
        this.island = island;
        this.identityName = identityName;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        this.shape = shape;
        this.id = id;

        defaultMessages = mineCity.defaultPlotFlags.getDefaultMessages();
        loadSimplePermissions();
        loadExceptPermissions();
    }

    @Slow
    public Plot(@NotNull ICityStorage storage, IExceptPermissionStorage permissionStorage,
                @NotNull Island island, @NotNull String identityName, @NotNull String name, @Nullable PlayerID owner,
                @NotNull BlockPos spawn, @NotNull Shape shape)
            throws DataSourceException
    {
        this.permissionStorage = permissionStorage;
        this.storage = storage;
        this.island = island;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        this.shape = shape;
        this.identityName = identityName;
        this.id = storage.createPlot(this);

        MineCity mineCity = island.getCity().mineCity;

        try
        {
            defaultMessages = mineCity.defaultPlotFlags.getDefaultMessages();
            denyAll(mineCity.defaultPlotFlags);
        }
        catch(UncheckedDataSourceException e)
        {
            System.err.println("[MineCity][SQL] Exception applying the default plot flags!");
            e.getCause().printStackTrace(System.err);
        }

        shape.chunks(island.world).forEach(mineCity::reloadChunkSlowly);
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action)
    {
        if(invalid)
            return Optional.of(INCONSISTENT_PLOT_MESSAGE);

        if(identity.getType() == Identity.Type.NATURE)
            return Optional.of(new Message("Plots are protected from natural actions"));

        if(owner != null)
        {
            if(identity.equals(owner))
                return Optional.empty();

            return super.can(identity, action);
        }

        OptionalPlayer cityOwner = island.getCity().owner();
        if(cityOwner.getType() == Identity.Type.ADMINS)
        {
            if(admin().equals(identity))
                return Optional.empty();
        }
        else if(identity.equals(cityOwner))
            return Optional.empty();

        Status status = strictPermission.getOrDefault(action, emptyMap()).get(identity);
        if(status != null)
        {
            if(status.message != null)
                return Optional.of(status.message);

            if(action.canBypass)
                return Optional.empty();
        }

        Message message = generalPermissions.get(action);
        if(message != null)
            return Optional.of(message);

        return island.getCity().can(identity, action);
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        if(invalid)
            return Optional.of(INCONSISTENT_PLOT_MESSAGE);

        Identity<UUID> identity = entity.getIdentity();
        if(owner != null)
        {
            if(identity.equals(owner))
                return Optional.empty();

            return super.can(entity, action);
        }

        if(identity.equals(island.getCity().owner()))
            return Optional.empty();

        Status status = getDirectPermission(entity, action);
        if(status != null)
        {
            if(status.message != null)
                return Optional.of(status.message);

            if(action.canBypass)
                return Optional.empty();
        }

        Message message = generalPermissions.get(action);
        if(message != null)
            return Optional.of(message);

        return island.getCity().can(identity, action);
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
    public void setShape(@NotNull Shape shape, BlockPos spawn) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        Shape old = this.shape;
        storage.setShape(this, shape, spawn);
        this.shape = shape;

        Stream.concat(this.shape.chunks(island.world), old.chunks(island.world))
                .distinct().forEach(getCity().mineCity::reloadChunkSlowly);
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
     */
    @Override
    @NotNull
    public OptionalPlayer owner()
    {
        if(owner != null)
            return owner;

        OptionalPlayer cityOwner = island.getCity().owner();
        if(cityOwner.getType() != Identity.Type.ADMINS)
            return cityOwner;

        return admin();
    }

    private AdminPlot admin()
    {
        if(adminOwner != null)
            return adminOwner;

        return adminOwner = new AdminPlot(this);
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

    @Override
    public String toString()
    {
        return "Plot{" +
                "id=" + id +
                ", island=" + island +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                '}';
    }
}
