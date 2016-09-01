package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.UUID;

public class ProjectileShooter implements Serializable
{
    private static final long serialVersionUID = 2391306059194864630L;

    @Nullable
    private transient IEntity entity;

    @Nullable
    private transient IEntity indirectEntity;

    @Nullable
    private Identity<?> identity;

    @Nullable
    private Identity<?> indirectId;

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

        indirectEntity = entity.getEntityOwner();
        if(indirectEntity != null)
            indirectId = indirectEntity.identity();
        else
        {
            UUID uuid = entity.getEntityOwnerId();
            if(uuid != null)
                indirectId = new PlayerID(uuid, "???");
        }
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

    @Nullable
    public IEntity getIndirectEntity()
    {
        return indirectEntity;
    }

    @Nullable
    public Identity<?> getIndirectId()
    {
        return indirectId;
    }

    @Nullable
    public Permissible getResponsible()
    {
        return indirectEntity != null? indirectEntity : indirectId != null? indirectId : entity != null? entity : identity;
    }
}
