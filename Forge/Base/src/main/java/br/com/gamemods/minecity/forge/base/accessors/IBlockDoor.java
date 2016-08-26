package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockDoorTransformer;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import br.com.gamemods.minecity.forge.base.protection.SingleBlockReaction;

@Referenced(at = BlockDoorTransformer.class)
public interface IBlockDoor extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.CLICK);
        reaction.addDenialListener((r, permissible, flag, p, message) ->
            player.getMineCityPlayer().getServer().callSyncMethod(()-> {
                player.sendBlock(p.x, p.y+1, p.z);
                player.sendBlock(p.x, p.y-1, p.z);
            })
        );
        return reaction;
    }
}
