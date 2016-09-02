package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockStemTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostBlockStemTransformer extends BlockStemTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostBlockStemTransformer()
    {
        super(MineCityFrostHooks.class.getName());
    }
}
