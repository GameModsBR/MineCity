package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockCropsTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenBlockCropsTransformer extends BlockCropsTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenBlockCropsTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockCrops");
    }
}
