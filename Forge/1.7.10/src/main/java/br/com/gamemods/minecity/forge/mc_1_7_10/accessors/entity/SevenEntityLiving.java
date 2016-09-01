package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLiving;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityLiving extends SevenEntityLivingBase, IEntityLiving
{
    @Override
    default boolean isNamed()
    {
        return getForgeEntity().hasCustomNameTag();
    }
}
