package br.com.gamemods.minecity.api;

import br.com.gamemods.minecity.api.permission.Identity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerID extends Identity<UUID>
{
    public PlayerID(@NotNull UUID uniqueId, @NotNull String name)
    {
        super(uniqueId, name);
    }

    public PlayerID(int dataSourceId, @NotNull UUID uniqueId, @NotNull String name)
    {
        super(uniqueId, name);
        setDataSourceId(dataSourceId);
    }

    @Override
    public Type getType()
    {
        return Type.PLAYER;
    }

    @Override
    public String toString()
    {
        return "PlayerID{" +
                "name='" + name + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
