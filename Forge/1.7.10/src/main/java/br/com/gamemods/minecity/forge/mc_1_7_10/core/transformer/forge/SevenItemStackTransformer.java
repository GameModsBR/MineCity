package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemStackTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenItemStackTransformer extends ItemStackTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenItemStackTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenItemStack");
    }
}
