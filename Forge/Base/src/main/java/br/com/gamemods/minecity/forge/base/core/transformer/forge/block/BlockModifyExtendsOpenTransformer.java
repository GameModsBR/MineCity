package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.Arrays;

@Referenced
public class BlockModifyExtendsOpenTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockModifyExtendsOpenTransformer()
    {
        super("br.com.gamemods.minecity.forge.base.accessors.block.IBlockModifyExtendsOpen", Arrays.asList(
                "net.minecraft.block.BlockCommandBlock",
                "net.minecraft.block.BlockEndPortal",
                "net.minecraft.block.BlockDaylightDetector",
                "net.minecraft.block.BlockFlowerPot",
                "net.minecraft.block.BlockNote",
                "net.minecraft.block.BlockStructure"
        ));
    }
}
