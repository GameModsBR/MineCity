package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockTNTTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostBlockTNTTransformer extends BlockTNTTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostBlockTNTTransformer()
    {
        super(MineCityFrostHooks.class.getName());
    }
}
