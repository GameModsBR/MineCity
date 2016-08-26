package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockCrops;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.block.SevenBlockCropsTransformer;
import net.minecraft.block.BlockCrops;

@Referenced(at = SevenBlockCropsTransformer.class)
public interface SevenBlockCrops extends IBlockCrops, SevenBlock
{
    @Override
    default BlockCrops getForgeBlock()
    {
        return (BlockCrops) this;
    }

    @Override
    default IItem getISeed()
    {
        return (IItem) ((BlockCrops) this).getItemDropped(0, null, 0);
    }

    @Override
    default int getMaxAge()
    {
        return 7;
    }
}
