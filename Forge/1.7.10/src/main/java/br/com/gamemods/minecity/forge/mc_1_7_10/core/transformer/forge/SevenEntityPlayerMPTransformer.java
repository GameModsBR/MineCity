package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.EntityPlayerMPTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenEntityPlayerMPTransformer extends EntityPlayerMPTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityPlayerMPTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenEntityPlayerMP");
    }
}
