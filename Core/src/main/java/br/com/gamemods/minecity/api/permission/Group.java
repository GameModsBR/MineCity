package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.structure.City;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Group implements Identifiable<Integer>
{
    public final int id;
    @NotNull
    private final ICityStorage storage;
    @NotNull
    public final City home;
    @NotNull
    private String name;
    @NotNull
    private final Set<Identity<?>> members;
    @NotNull
    private final GroupID identity;
    private boolean invalid;

    public Group(@NotNull ICityStorage storage, int id, @NotNull City home, @NotNull String name,
                 @NotNull Set<Identity<?>> members)
    {
        this.id = id;
        this.home = home;
        this.name = name;
        this.members = Collections.newSetFromMap(new ConcurrentHashMap<>(members.size()));
        this.members.addAll(members);
        this.storage = storage;
        identity = new GroupID(id, name, home.getName(), home.getId());
    }

    public synchronized void addMember(@NotNull Identity<?> member) throws DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        if(members.contains(member))
            return;

        storage.addMember(this, member);
        members.add(member);
    }

    public synchronized void removeMember(@NotNull Identity<?> member) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        if(!members.contains(member))
            return;

        storage.removeMember(this, member);
        members.remove(member);
    }

    public boolean hasMember(@NotNull Identity<?> member)
    {
        return !invalid && members.contains(member);
    }

    @NotNull
    public Set<Identity<?>> getMembers()
    {
        if(invalid)
            return Collections.emptySet();

        return Collections.unmodifiableSet(members);
    }

    public void setName(@NotNull String name) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        Group group = home.getGroup(name);
        if(group != null)
            throw new IllegalArgumentException("'"+name+"' conflicts with '"+group.name+"'");

        String id = StringUtil.identity(name);

        storage.setName(this, id, name);

        identity.name = name;
        this.name = name;
    }

    public void remove() throws DataSourceException
    {
        if(invalid)
            return;

        storage.deleteGroup(this);
        invalid = true;
        members.clear();
    }

    public boolean isValid()
    {
        return !invalid;
    }

    public void updateCityName()
    {
        identity.home = home.getName();
    }

    @NotNull
    @Override
    public Integer getUniqueId()
    {
        return id;
    }

    @Override
    @NotNull
    public String getName()
    {
        return name;
    }

    @NotNull
    @Override
    public GroupID getIdentity()
    {
        return identity;
    }
}
