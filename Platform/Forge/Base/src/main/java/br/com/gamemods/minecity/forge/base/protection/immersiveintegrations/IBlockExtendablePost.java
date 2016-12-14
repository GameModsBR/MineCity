package br.com.gamemods.minecity.forge.base.protection.immersiveintegrations;

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
import br.com.gamemods.minecity.forge.base.protection.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockExtendablePost extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        if(stack == null)
            return NoReaction.INSTANCE;

        if(stack.isTool("IE_HAMMER"))
        {
            int meta = state.getIntValueOrMeta("metadata");
            if(meta != 2 && meta != 1)
            {
                if(meta != 0)
                    return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
            }
            else
            {
                return new DoubleBlockReaction(PermissionFlag.MODIFY, pos, pos.add(face));
            }
        }
        else if("tile.ImmersiveEngineering.woodenDecoration.fence".equals(stack.getUnlocalizedName()))
        {
            IWorldServer world = pos.world.getInstance(IWorldServer.class);
            int maxHeight = world.getActualMaxHeight();
            for(int y = pos.y; y < maxHeight; y++)
            {
                if(world.isAir(pos.x, y, pos.z))
                    return new SingleBlockReaction(new BlockPos(pos, pos.x, y, pos.z), PermissionFlag.MODIFY);

                if(world.getIBlock(pos.x, y, pos.z) != this)
                    return NoReaction.INSTANCE;
            }
        }

        return NoReaction.INSTANCE;
    }
}
