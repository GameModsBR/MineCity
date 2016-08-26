package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class BlockTNTTransformer extends InsertInterfaceTransformer
{
    public BlockTNTTransformer(String interfaceClass)
    {
        super("net.minecraft.block.BlockTNT", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockTNTTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.block.IBlockTNT");
    }
}
