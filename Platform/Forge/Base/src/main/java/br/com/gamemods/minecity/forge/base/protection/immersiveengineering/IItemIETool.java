package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemIETool extends IItemIEBase
{
    @Override
    default Reaction reactRightClickBlockFirstUse(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                                  IState state, BlockPos pos, Direction face)
    {
        switch(stack.getMeta())
        {
            case 0:
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

            case 1:
            {
                ITileEntity tile = player.getIWorld().getTileEntity(pos);
                if(tile instanceof IConnectable)
                    return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
                return NoReaction.INSTANCE;
            }

            case 2:
                return new SingleBlockReaction(pos, PermissionFlag.CLICK);

            default:
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        }
    }
}
