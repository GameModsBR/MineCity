package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class BlockDoorTransformer extends InsertInterfaceTransformer
{
    public BlockDoorTransformer(String interfaceClass)
    {
        super("net.minecraft.block.BlockDoor", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BlockDoorTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.IBlockDoor");
    }
}
