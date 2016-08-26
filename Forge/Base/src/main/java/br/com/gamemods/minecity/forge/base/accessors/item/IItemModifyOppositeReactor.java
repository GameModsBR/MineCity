package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemModifyOppositeReactorTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ItemModifyOppositeReactorTransformer.class)
public interface IItemModifyOppositeReactor extends IItem
{
    @Override
    default Reaction react(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos,
                           Direction face)
    {
        return new SingleBlockReaction(pos.add(face.getOpposite()), PermissionFlag.MODIFY);
    }
}
