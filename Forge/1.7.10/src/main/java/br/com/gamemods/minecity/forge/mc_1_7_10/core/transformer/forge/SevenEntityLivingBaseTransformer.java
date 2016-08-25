package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.EntityLivingBaseTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenEntityLivingBaseTransformer extends EntityLivingBaseTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityLivingBaseTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenEntityLivingBase");
    }
}
