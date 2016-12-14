package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.EntityProjectile;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityIC2ExplosiveTransformer;
import org.jetbrains.annotations.Nullable;

@Referenced(at = EntityIC2ExplosiveTransformer.class)
public interface IEntityIC2Explosive extends EntityProjectile
{
    @Referenced(at = EntityIC2ExplosiveTransformer.class)
    IEntityLivingBase getIgniter();

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        return getIgniter();
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase owner = getIgniter();
        if(owner == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), owner));
    }
}
