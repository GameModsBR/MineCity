package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.entity.projectile.EntityThrowable;

public interface IEntityThrowable extends EntityProjectile
{
    @Override
    default EntityThrowable getForgeEntity()
    {
        return (EntityThrowable) this;
    }

    default IEntityLivingBase getIThrower()
    {
        return (IEntityLivingBase) ((EntityThrowable) this).getThrower();
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase thrower = getIThrower();
        if(thrower == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), thrower));
    }
}
