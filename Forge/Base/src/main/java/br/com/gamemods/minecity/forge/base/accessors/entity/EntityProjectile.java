package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface EntityProjectile extends Projectile, IEntity
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.PROJECTILE;
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        setShooter(new ProjectileShooter(getEntityPos(mod)));
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        ProjectileShooter shooter = getShooter();
        if(shooter == null)
            return null;

        Identity<?> identity = shooter.getIdentity();
        if(identity == null || !(identity.uniqueId instanceof UUID))
            return null;

        return (UUID) identity.uniqueId;
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        ProjectileShooter shooter = getShooter();
        if(shooter == null)
            return null;

        IEntity entity = shooter.getEntity();
        if(entity != null)
        {
            if(entity instanceof IEntityLivingBase)
                return (IEntityLivingBase) entity;
            return null;
        }

        Identity<?> identity = shooter.getIdentity();
        if(identity == null || !(identity.uniqueId instanceof UUID))
            return null;

        World world = getForgeEntity().worldObj;
        return (IEntityLivingBase) world.getPlayerEntityByUUID((UUID) identity.uniqueId);
    }
}
