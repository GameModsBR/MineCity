package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.world.EntityPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ProjectileShooter implements Serializable
{
    private static final long serialVersionUID = 2391306059194864630L;

    @Nullable
    private transient IEntity entity;

    @Nullable
    private Identity<?> identity;

    @NotNull
    private EntityPos pos;

    public ProjectileShooter(@NotNull EntityPos pos)
    {
        this.pos = pos;
    }

    public ProjectileShooter(@NotNull EntityPos pos, @NotNull IEntity entity)
    {
        this.entity = entity;
        this.pos = pos;
        this.identity = entity.identity();
    }

    public ProjectileShooter(@NotNull EntityPos pos, @NotNull Identity<?> identity)
    {
        this.identity = identity;
        this.pos = pos;
    }

    @Nullable
    public IEntity getEntity()
    {
        return entity;
    }

    @Nullable
    public Identity<?> getIdentity()
    {
        return identity;
    }

    @NotNull
    public EntityPos getPos()
    {
        return pos;
    }
}
