package br.com.gamemods.minecity.api;

import br.com.gamemods.minecity.api.permission.Identity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerID extends Identity<UUID>
{
    private int dataSourceId;

    public PlayerID(@NotNull UUID uniqueId, @NotNull String name)
    {
        super(uniqueId, name);
    }

    public PlayerID(int dataSourceId, @NotNull UUID uniqueId, @NotNull String name)
    {
        super(uniqueId, name);
        setDataSourceId(dataSourceId);
    }

    public int getDataSourceId()
    {
        return dataSourceId;
    }

    public void setDataSourceId(int id) throws IllegalStateException, IllegalArgumentException
    {
        if(id < 0) throw new IllegalArgumentException();
        if(dataSourceId > 0) throw new IllegalStateException();
        dataSourceId = id;
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
