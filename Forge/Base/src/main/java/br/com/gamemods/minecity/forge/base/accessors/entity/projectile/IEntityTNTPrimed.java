package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.item.EntityTNTPrimed;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityTNTPrimed extends EntityProjectile
{
    @Override
    default EntityTNTPrimed getForgeEntity()
    {
        return (EntityTNTPrimed) this;
    }

    default IEntityLivingBase getPlacedBy()
    {
        return (IEntityLivingBase) ((EntityTNTPrimed) this).getTntPlacedBy();
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase placedBy = getPlacedBy();
        if(placedBy == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), placedBy));
    }
}
