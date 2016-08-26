package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.Arrays;

@Referenced
public class BlockClickExtendsOpenTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockClickExtendsOpenTransformer()
    {
        super("br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen", Arrays.asList(
                "net.minecraft.block.BlockEnderChest",
                "net.minecraft.block.BlockEnchantmentTable"
        ));
    }
}
