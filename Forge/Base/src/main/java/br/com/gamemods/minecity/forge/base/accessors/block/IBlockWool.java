package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IBlockWool extends IBlock
{
    @Override
    default boolean isHarvest()
    {
        return true;
    }
}
