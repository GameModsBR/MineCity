package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.EntityProjectile;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityDynamiteTransformer;
import org.jetbrains.annotations.Nullable;

@Referenced(at = EntityDynamiteTransformer.class)
public interface IEntityDynamite extends EntityProjectile
{
    @Referenced(at = EntityDynamiteTransformer.class)
    IEntityLivingBase getOwner();

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        return getOwner();
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase owner = getOwner();
        if(owner == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), owner));
    }
}
