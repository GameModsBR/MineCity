package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.Arrays;

@Referenced
public class BlockNoReactExtendsOpenTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockNoReactExtendsOpenTransformer()
    {
        super("br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen", Arrays.asList(
                "net.minecraft.block.BlockBanner",
                "net.minecraft.block.BlockSkull",
                "net.minecraft.block.BlockSign",
                "net.minecraft.block.BlockPistonMoving",
                "net.minecraft.block.BlockMobSpawner"
        ));
    }
}
