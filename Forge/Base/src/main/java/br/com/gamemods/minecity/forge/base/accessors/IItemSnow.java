package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemSnowTransformer;
import br.com.gamemods.minecity.forge.base.protection.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import br.com.gamemods.minecity.forge.base.protection.SingleBlockReaction;

@Referenced(at = ItemSnowTransformer.class)
public interface IItemSnow extends IItemBlock
{
    @Override
    default Reaction react(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos,
                           Direction face)
    {
        IBlock block = state.getIBlock();
        IBlock snow = this.getIBlock();

        if((face != Direction.UP || block != snow) && !block.isReplaceable(pos))
        {
            pos = pos.add(face);
            assert pos.world.instance != null;
            state = ((IWorldServer)pos.world.instance).getIState(pos);
            block = state.getIBlock();
        }

        if(block == snow)
        {
            int i = state.getIntValueOrMeta("layers");

            if(i <= 7)
            {
                SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.MODIFY);
                reaction.onDenyUpdateInventory();
                return reaction;
            }
        }

        return NoReaction.INSTANCE;
    }
}
