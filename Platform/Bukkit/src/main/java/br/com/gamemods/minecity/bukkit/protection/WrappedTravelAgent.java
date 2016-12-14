package br.com.gamemods.minecity.bukkit.protection;

import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class WrappedTravelAgent implements TravelAgent
{
    @NotNull
    protected TravelAgent source;

    public WrappedTravelAgent(@NotNull TravelAgent source)
    {
        this.source = source;
    }

    @NotNull
    @Override
    public TravelAgent setSearchRadius(int radius)
    {
        source.setSearchRadius(radius);
        return this;
    }

    @Override
    public int getSearchRadius()
    {
        return source.getSearchRadius();
    }

    @NotNull
    @Override
    public TravelAgent setCreationRadius(int radius)
    {
        source.setCreationRadius(radius);
        return this;
    }

    @Override
    public int getCreationRadius()
    {
        return source.getCreationRadius();
    }

    @Override
    public boolean getCanCreatePortal()
    {
        return source.getCanCreatePortal();
    }

    @Override
    public void setCanCreatePortal(boolean create)
    {
        source.setCanCreatePortal(create);
    }

    @Override
    public Location findOrCreate(@NotNull Location location)
    {
        Location portal = findPortal(location);
        if(portal != null)
            return portal;

        if(getCanCreatePortal() && createPortal(location))
            return findPortal(location);

        return location;
    }

    @Nullable
    @Override
    public Location findPortal(@NotNull Location location)
    {
        return source.findPortal(location);
    }

    @Override
    public boolean createPortal(@NotNull Location location)
    {
        return source.createPortal(location);
    }
}
