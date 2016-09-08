package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.item.ItemBlock;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItemBlock extends ItemBlockBase
{
    @Override
    default ItemBlock getForgeItem()
    {
        return (ItemBlock) this;
    }

    @Override
    default IBlock getIBlock()
    {
        return (IBlock) ((ItemBlock) this).getBlock();
    }

    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        return getIBlock().reactRightClickAsItem(player, stack, offHand, state, pos, face);
    }
}
