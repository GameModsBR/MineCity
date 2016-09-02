package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityPlayerMPTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenEntityPlayerMPTransformer extends EntityPlayerMPTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityPlayerMPTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityPlayerMP");
    }
}
