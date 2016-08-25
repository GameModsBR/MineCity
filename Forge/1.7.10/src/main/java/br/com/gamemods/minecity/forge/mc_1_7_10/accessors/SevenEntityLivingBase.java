package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IEntityLivingBase;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenEntityLivingBaseTransformer;

@Referenced(at = SevenEntityLivingBaseTransformer.class)
public interface SevenEntityLivingBase extends IEntityLivingBase
{
    @Override
    default boolean isElytraFlying()
    {
        return false;
    }
}
