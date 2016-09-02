package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityPotionTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenEntityPotionTransformer extends EntityPotionTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityPotionTransformer()
    {
        super(0);
    }
}
