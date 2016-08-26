package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockNoReactExtendsOpenTransformer;
import br.com.gamemods.minecity.forge.base.protection.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.Reaction;

@Referenced(at = BlockNoReactExtendsOpenTransformer.class)
public interface IBlockNoReactionExtendsOpen extends IBlockOpenReactor
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        return NoReaction.INSTANCE;
    }
}
