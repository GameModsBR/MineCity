package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.INatureStorage;
import br.com.gamemods.minecity.datasource.api.ISimplePermissionStorage;
import br.com.gamemods.minecity.datasource.api.SimpleStorageHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Nature extends SimpleStorageHolder implements ChunkOwner
{
    @NotNull
    private final INatureStorage storage;

    @NotNull
    public final MineCity mineCity;

    @NotNull
    public final WorldDim world;

    private boolean valid = true;
    private boolean denyCityCreation;

    public Nature(@NotNull MineCity mineCity, @NotNull WorldDim world,
                  @NotNull INatureStorage storage, @NotNull ISimplePermissionStorage permissionStorage)
    {
        this.storage = storage;
        this.permissionStorage = permissionStorage;
        this.mineCity = mineCity;
        this.world = world;
        denyAll(mineCity.defaultNatureFlags);
    }

    public Nature(@NotNull MineCity mineCity, @NotNull WorldDim world, @Nullable Message defaultDenialMessage,
                  @NotNull INatureStorage storage, @NotNull ISimplePermissionStorage permissionStorage,
                  boolean denyCityCreation)
            throws DataSourceException
    {
        super(defaultDenialMessage);
        this.mineCity = mineCity;
        this.permissionStorage = permissionStorage;
        this.storage = storage;
        this.world = world;
        this.denyCityCreation = denyCityCreation;

        loadPermissions();
    }

    @Slow
    public void setName(String name) throws DataSourceException
    {
        if(!valid)
            throw new IllegalStateException();

        storage.setName(this, name);
        world.name = name;
    }

    @Slow
    public void setCityCreationDenied(boolean denied)
            throws IllegalStateException, DataSourceException
    {
        if(!valid)
            throw new IllegalStateException();

        storage.setCityCreationDenied(this, denied);
        denyCityCreation = denied;
    }

    public boolean isCityCreationDenied()
    {
        return denyCityCreation;
    }

    public void invalidate()
    {
        valid = false;
    }

    public boolean isValid()
    {
        return valid;
    }
}
