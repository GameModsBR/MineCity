package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.projectile.EntityArrow;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityArrow extends EntityProjectile
{
    @Override
    default EntityArrow getForgeEntity()
    {
        return (EntityArrow) this;
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntity shooter = (IEntity) getForgeEntity().shootingEntity;
        if(shooter == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), shooter));
    }
}
