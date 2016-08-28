package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityID extends Identity<UUID>
{
    private static final long serialVersionUID = -2507233509584643722L;
    public final MinecraftEntity.Type type;

    public EntityID(@NotNull MinecraftEntity.Type type, @NotNull UUID id, @NotNull String name)
    {
        super(id, name);
        this.type = type;
    }

    public EntityID(int dataSourceId, @NotNull MinecraftEntity.Type type, @NotNull UUID id, @NotNull String name)
    {
        super(id, name);
        this.type = type;
        setDataSourceId(dataSourceId);
    }

    @Override
    public Type getType()
    {
        return Type.ENTITY;
    }

    public MinecraftEntity.Type getEntityType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return "EntityID{" +
                "name='" + getName() + '\'' +
                ", type='" + type + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
