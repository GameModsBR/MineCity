package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface ThrowableEntity extends EntityProjectile
{
    default IEntity getThrower()
    {
        return (IEntity) ((IThrowableEntity) this).getThrower();
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntity shooter = getThrower();
        if(shooter == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), shooter));
    }
}
