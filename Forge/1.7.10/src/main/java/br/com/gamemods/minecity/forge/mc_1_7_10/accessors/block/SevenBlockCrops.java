package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockCrops;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.block.BlockCrops;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenBlockCrops extends IBlockCrops, SevenBlock
{
    @Override
    default BlockCrops getForgeBlock()
    {
        return (BlockCrops) this;
    }

    @Override
    default int getMaxAge()
    {
        return 7;
    }
}
