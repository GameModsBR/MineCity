package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockScaffold extends IBlock
{
    @Override
    default Reaction reactLeftClick(IEntityPlayerMP player, IState state, BlockPos pos, Direction face,
                                    IItemStack stack, boolean offHand)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        int y = pos.y;
        while(y < 255)
        {
            if(!world.getIState(pos.x, ++y, pos.z).equals(state))
                break;
        }

        return new SingleBlockReaction(new BlockPos(pos, pos.x, y, pos.z), PermissionFlag.MODIFY).onDenyUpdateBlock(player);
    }
}
