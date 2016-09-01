package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.projectile.EntityFireball;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityFireball extends EntityProjectile
{
    @Override
    default EntityFireball getForgeEntity()
    {
        return (EntityFireball) this;
    }

    default IEntityLivingBase getIShooter()
    {
        return (IEntityLivingBase) ((EntityFireball) this).shootingEntity;
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase shooter = getIShooter();
        if(shooter == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), shooter));
    }
}
