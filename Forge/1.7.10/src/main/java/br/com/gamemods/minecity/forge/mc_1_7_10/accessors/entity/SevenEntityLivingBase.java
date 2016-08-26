package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityLivingBase;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityLivingBaseTransformer;

@Referenced(at = SevenEntityLivingBaseTransformer.class)
public interface SevenEntityLivingBase extends IEntityLivingBase, SevenEntity
{
    @Override
    default boolean isElytraFlying()
    {
        return false;
    }
}
