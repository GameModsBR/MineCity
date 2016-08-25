package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.EntityTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostEntityTransformer extends EntityTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostEntity");
    }
}
