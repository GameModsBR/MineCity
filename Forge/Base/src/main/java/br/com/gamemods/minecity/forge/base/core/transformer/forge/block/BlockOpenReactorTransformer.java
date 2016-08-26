package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.Arrays;

@Referenced
public class BlockOpenReactorTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockOpenReactorTransformer()
    {
        super("br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor", Arrays.asList(
                "net.minecraft.block.BlockContainer",
                "net.minecraft.block.BlockAnvil"
        ));
    }
}
