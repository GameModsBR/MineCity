package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.block.BlockCrops;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IBlockCrops extends SimpleCrop
{
    @Override
    default BlockCrops getForgeBlock()
    {
        return (BlockCrops) this;
    }

    default int getMaxAge()
    {
        return getForgeBlock().getMaxAge();
    }

    @Override
    default boolean isHarvestAge(int age)
    {
        return age == getMaxAge();
    }
}
