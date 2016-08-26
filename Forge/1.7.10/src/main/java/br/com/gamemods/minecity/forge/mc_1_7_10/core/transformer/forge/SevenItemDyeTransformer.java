package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemDyeTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenItemDyeTransformer extends ItemDyeTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenItemDyeTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenItemDye");
    }
}
