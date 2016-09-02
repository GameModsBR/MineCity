package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.projectile.EntityThrowable;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ForgeInterfaceTransformer.class)
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

    @NotNull
    @Override
    default Type getType()
    {
        return Type.PROJECTILE;
    }
}
