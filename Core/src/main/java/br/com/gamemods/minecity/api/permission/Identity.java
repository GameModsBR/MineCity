package br.com.gamemods.minecity.api.permission;

import org.jetbrains.annotations.NotNull;

public abstract class Identity<T> implements Permissible, Comparable<Identity<?>>
{
    private int dataSourceId;
    @NotNull
    public final T uniqueId;

    @NotNull
    private String name;

    public Identity(@NotNull T id, @NotNull String name)
    {
        this.uniqueId = id;
        this.name = name;
    }

    public abstract Type getType();

    public void setDataSourceId(int id) throws IllegalStateException, IllegalArgumentException
    {
        if(id < 0) throw new IllegalArgumentException();
        if(dataSourceId > 0) throw new IllegalStateException();
        dataSourceId = id;
    }

    public int getDataSourceId()
    {
        return dataSourceId;
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
        int i = getType().compareTo(o.getType());
        if(i != 0) return i;
        i = getName().compareToIgnoreCase(o.getName());
        return i != 0? i : uniqueId.toString().compareToIgnoreCase(o.uniqueId.toString());
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
        return getClass().getSimpleName()+"{" +
                "name='" + getName() + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }

    public void setName(@NotNull String name)
    {
        this.name = name;
    }

    @NotNull
    @Override
    final public Identity<T> identity()
    {
        return this;
    }

    public enum Type
    {
        PLAYER, GROUP, ENTITY, NATURE, ADMINS
    }
}
