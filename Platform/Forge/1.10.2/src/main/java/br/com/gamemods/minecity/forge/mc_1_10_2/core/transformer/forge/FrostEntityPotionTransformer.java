package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityPotionTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostEntityPotionTransformer extends EntityPotionTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityPotionTransformer()
    {
        super(1);
    }
}
