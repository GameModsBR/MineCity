package br.com.gamemods.minecity.api.world;

import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.permission.Identity;

public class EntityUpdate
{
    public final Identity<?> identity;
    public final Type type;
    public final GroupID groupId;
    public int ticks = 3;

    public EntityUpdate(Identity<?> identity, Type type, GroupID groupId)
    {
        this.identity = identity;
        this.type = type;
        this.groupId = groupId;
    }

    public enum Type
    {
        GROUP_ADDED, GROUP_REMOVED
    }
}
