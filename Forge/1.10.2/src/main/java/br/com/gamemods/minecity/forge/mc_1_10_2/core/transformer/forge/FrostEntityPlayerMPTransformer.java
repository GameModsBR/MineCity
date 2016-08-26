package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityPlayerMPTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostEntityPlayerMPTransformer extends EntityPlayerMPTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityPlayerMPTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.entity.FrostEntityPlayerMP");
    }
}
