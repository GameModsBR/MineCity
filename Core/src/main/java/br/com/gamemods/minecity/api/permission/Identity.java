package br.com.gamemods.minecity.api.permission;

import org.jetbrains.annotations.NotNull;

public class Identity<T> implements Comparable<Identity<?>>
{
    @NotNull
    public final T uniqueId;

    @NotNull
    public String name;

    public Identity(@NotNull T id, @NotNull String name)
    {
        this.uniqueId = id;
        this.name = name;
    }

    @NotNull
    public T getUniqueId()
    {
        return uniqueId;
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    @Override
    public int compareTo(@NotNull Identity<?> o)
    {
        int i = name.compareToIgnoreCase(o.name);
        return i == 0? uniqueId.toString().compareToIgnoreCase(o.uniqueId.toString()) : i;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Identity<?> identity = (Identity<?>) o;
        return uniqueId.equals(identity.uniqueId);
    }

    @Override
    public int hashCode()
    {
        return uniqueId.hashCode();
    }

    @Override
    public String toString()
    {
        return "Identity{" +
                "name='" + name + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
