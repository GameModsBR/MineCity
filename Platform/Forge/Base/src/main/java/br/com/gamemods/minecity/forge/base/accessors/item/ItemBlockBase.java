package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

public interface ItemBlockBase extends IItem
{
    IBlock getIBlock();

    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return getIBlock().isHarvest();
    }

    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        return getIBlock().reactPrePlace(who, stack, pos);
    }
}
