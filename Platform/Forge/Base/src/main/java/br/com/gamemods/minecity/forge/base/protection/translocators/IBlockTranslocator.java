package br.com.gamemods.minecity.forge.base.protection.translocators;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockTranslocator extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactRightClickAsItem(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                           IState state, BlockPos pos, Direction face)
    {
        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        reaction.addDenialListener((reaction1, permissible, flag, pos1, message) ->
                player.sendBlockAndTile(pos.add(face))
        );
        return reaction;
    }
}
