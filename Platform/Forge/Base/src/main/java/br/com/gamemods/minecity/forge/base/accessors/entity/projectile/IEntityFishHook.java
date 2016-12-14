package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.projectile.EntityFishHook;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityFishHook extends EntityProjectile
{
    @Override
    default EntityFishHook getForgeEntity()
    {
        return (EntityFishHook) this;
    }

    default IEntityPlayerMP getAnger()
    {
        return (IEntityPlayerMP) ((EntityFishHook) this).angler;
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityPlayerMP anger = getAnger();
        if(anger == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), anger));
    }
}
