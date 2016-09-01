package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockDragonEggTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced(at = MineCityFrostCoreMod.class)
public class FrostBlockDragonEggTransformer extends BlockDragonEggTransformer
{
    public FrostBlockDragonEggTransformer()
    {
        super(MineCityFrostHooks.class.getName());
    }
}
