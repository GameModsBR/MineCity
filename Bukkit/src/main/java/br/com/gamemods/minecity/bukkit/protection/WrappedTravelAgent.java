package br.com.gamemods.minecity.bukkit.protection;

import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedTravelAgent implements TravelAgent
{
    @NotNull
    protected TravelAgent source;

    public WrappedTravelAgent(@NotNull TravelAgent source)
    {
        this.source = source;
    }

    @Override
    public TravelAgent setSearchRadius(int radius)
    {
        return source.setSearchRadius(radius);
    }

    @Override
    public int getSearchRadius()
    {
        return source.getSearchRadius();
    }

    @Override
    public TravelAgent setCreationRadius(int radius)
    {
        return source.setCreationRadius(radius);
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
    public Location findOrCreate(Location location)
    {
        return source.findOrCreate(location);
    }

    @Override
    public Location findPortal(Location location)
    {
        return source.findPortal(location);
    }

    @Override
    public boolean createPortal(Location location)
    {
        return source.createPortal(location);
    }
}
