package br.com.gamemods.minecity.api.permission;

import org.jetbrains.annotations.NotNull;

public class GroupID extends Identity<Integer>
{
    public final int homeId;
    public String home;
    public GroupID(int id, @NotNull String name, @NotNull String home, int homeId)
    {
        super(id, name);
        this.home = home;
        this.homeId = homeId;
        setDataSourceId(id);
    }

    @Override
    public Type getType()
    {
        return Type.GROUP;
    }

    @Override
    public String toString()
    {
        return "GroupID{" +
                "home='" + home + '\'' +
                ", name='" + getName() + '\'' +
                ", homeId=" + homeId + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
