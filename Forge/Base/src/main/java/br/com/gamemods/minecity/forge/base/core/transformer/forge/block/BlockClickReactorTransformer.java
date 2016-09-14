package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.Arrays;

@Referenced
public class BlockClickReactorTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockClickReactorTransformer()
    {
        super("br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickReactor", Arrays.asList(
                "net.minecraft.block.BlockLever",
                "net.minecraft.block.BlockButton",
                "net.minecraft.block.BlockTrapDoor",
                "net.minecraft.block.BlockFenceGate",
                "net.minecraft.block.BlockWorkbench"
        ));
    }
}
