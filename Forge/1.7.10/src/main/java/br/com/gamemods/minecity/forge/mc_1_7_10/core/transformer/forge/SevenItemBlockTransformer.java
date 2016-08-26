package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemBlockTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenItemBlockTransformer extends ItemBlockTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenItemBlockTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenItemBlock");
    }
}
