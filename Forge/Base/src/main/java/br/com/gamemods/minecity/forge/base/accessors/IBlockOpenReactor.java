package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.OpenReactorsTransformer;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import br.com.gamemods.minecity.forge.base.protection.SingleBlockReaction;

@Referenced(at = OpenReactorsTransformer.class)
public interface IBlockOpenReactor extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        return new SingleBlockReaction(pos, PermissionFlag.OPEN);
    }
}
