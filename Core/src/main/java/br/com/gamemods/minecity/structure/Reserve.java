package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class Reserve implements ChunkOwner, FlagHolder
{
    @NotNull
    public final Island island;

    public Reserve(@NotNull Island island)
    {
        this.island = island;
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        return island.getCity().mineCity.defaultReserveFlags.can(entity, action);
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action)
    {
        return island.getCity().mineCity.defaultReserveFlags.can(identity, action);
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Permissible permissible, @NotNull PermissionFlag action)
    {
        return island.getCity().mineCity.defaultReserveFlags.can(permissible, action);
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
}
