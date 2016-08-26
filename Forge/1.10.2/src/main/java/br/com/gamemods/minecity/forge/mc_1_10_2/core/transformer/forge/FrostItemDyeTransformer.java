package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemDyeTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostItemDyeTransformer extends ItemDyeTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostItemDyeTransformer(String interfaceClass)
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostItemDye");
    }
}
