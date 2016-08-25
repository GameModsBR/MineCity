package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostBlockTransformer extends BlockTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostBlockTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostBlock");
    }
}
