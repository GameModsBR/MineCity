package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.IBlockStateTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostIBlockStateTransformer extends IBlockStateTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostIBlockStateTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block.FrostState");
    }
}
