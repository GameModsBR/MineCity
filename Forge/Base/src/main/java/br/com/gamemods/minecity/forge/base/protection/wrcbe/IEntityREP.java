package br.com.gamemods.minecity.forge.base.protection.wrcbe;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.EntityProjectile;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbe.EntityREPTransformer;

@Referenced(at = EntityREPTransformer.class)
public interface IEntityREP extends EntityProjectile
{
    @Referenced(at = EntityREPTransformer.class)
    IEntityLivingBase getShooterEntity();

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase entity = getShooterEntity();
        if(entity != null)
            setShooter(new ProjectileShooter(getEntityPos(mod), entity));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod)));
    }
}
