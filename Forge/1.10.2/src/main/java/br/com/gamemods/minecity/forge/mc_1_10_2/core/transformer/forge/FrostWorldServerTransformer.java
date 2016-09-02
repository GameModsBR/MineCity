package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.world.WorldServerTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostWorldServerTransformer extends WorldServerTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostWorldServerTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostWorldServer");
    }
}
