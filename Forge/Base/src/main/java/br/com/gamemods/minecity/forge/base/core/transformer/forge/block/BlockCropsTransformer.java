package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class BlockCropsTransformer extends InsertInterfaceTransformer
{
    public BlockCropsTransformer(String interfaceClass)
    {
        super("net.minecraft.block.BlockCrops", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockCropsTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.block.IBlockCrops");
    }
}
