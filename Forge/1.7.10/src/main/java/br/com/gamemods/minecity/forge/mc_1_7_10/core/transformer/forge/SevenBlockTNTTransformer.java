package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockTNTTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.MineCitySevenHooks;

@Referenced
public class SevenBlockTNTTransformer extends BlockTNTTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenBlockTNTTransformer()
    {
        super(MineCitySevenHooks.class.getName());
    }
}
