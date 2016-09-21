package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class Reserve implements ChunkOwner, FlagHolder
{
    @NotNull
    public final Island island;
    private Message ownerNameCache;
    private byte ownerNameLife = Byte.MAX_VALUE;
    private String cityName;
    private Identity<?> cityOwner;

    public Reserve(@NotNull Island island)
    {
        this.island = island;
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        return island.getCity().mineCity.defaultReserveFlags.can(entity, action).map(msg-> mark(msg, action));
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action)
    {
        return island.getCity().mineCity.defaultReserveFlags.can(identity, action).map(msg-> mark(msg, action));
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Permissible permissible, @NotNull PermissionFlag action)
    {
        return island.getCity().mineCity.defaultReserveFlags.can(permissible, action).map(msg-> mark(msg, action));
    }

    @NotNull
    @Override
    public Identity<?> owner()
    {
        return island.getCity().mineCity.nature(island.world).owner();
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Reserve reserve = (Reserve) o;

        return island.equals(reserve.island);
    }

    @Override
    public int hashCode()
    {
        return island.hashCode();
    }

    @Override
    public String toString()
    {
        return "Reserve{" +
                "island=" + island +
                '}';
    }

    @Override
    public Message ownerName()
    {
        City city = island.getCity();
        String name = city.getName();
        Message cache = this.ownerNameCache;
        if(cache != null && --ownerNameLife > 0 && name.equals(cityName) && city.owner().equals(cityOwner))
            return this.ownerNameCache;

        cityName = name;
        ownerNameLife = 127;
        Message msg;
        OptionalPlayer owner = city.owner();
        if(owner.getType() == Identity.Type.ADMINS)
        {
            msg = new Message("action.denied.reserve.admin", "<msg><i>Reserved to ${name}</i></msg>", new Object[]{"name", name});
        }
        else
        {
            msg = new Message("action.denied.reserve.normal", "<msg><i>Reserved to ${name} ~ ${owner}</i></msg>", new Object[][]{
                    {"name", name}, {"owner", owner.getName()}
            });
        }

        return this.ownerNameCache = msg;
    }
}
