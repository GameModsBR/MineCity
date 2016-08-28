package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import org.jetbrains.annotations.NotNull;

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
}
