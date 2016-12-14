package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ThrowableEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import cpw.mods.fml.common.registry.IThrowableEntity;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenThrowableEntity extends ThrowableEntity
{
    @Override
    default IEntity getThrower()
    {
        return (IEntity)((IThrowableEntity) this).getThrower();
    }
}
