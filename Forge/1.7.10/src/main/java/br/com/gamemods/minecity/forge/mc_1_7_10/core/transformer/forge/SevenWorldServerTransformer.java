package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.WorldServerTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenWorldServerTransformer extends WorldServerTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenWorldServerTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenWorldServer");
    }
}
