package br.com.gamemods.minecity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerID implements Comparable<PlayerID>
{
    private int dataSourceId;

    @NotNull
    public final UUID uniqueId;
    @NotNull
    public String name;

    public PlayerID(@NotNull UUID uniqueId, @NotNull String name)
    {
        this.uniqueId = uniqueId;
        this.name = name;
    }

    public PlayerID(int dataSourceId, @NotNull UUID uniqueId, @NotNull String name)
    {
        this.uniqueId = uniqueId;
        this.name = name;
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
    public int compareTo(@NotNull PlayerID o)
    {
        return name.compareToIgnoreCase(o.name);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        PlayerID playerID = (PlayerID) o;
        return uniqueId.equals(playerID.uniqueId);
    }

    @Override
    public int hashCode()
    {
        return uniqueId.hashCode();
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
