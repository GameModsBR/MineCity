package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemFluidCell extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                                  IState state, BlockPos pos, Direction face)
    {
        IRayTraceResult trace = rayTrace(player.getIWorld(), player, true);
        if(trace == null)
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        if(trace.getHitType() == 1)
        {
            if(stack.getMeta() == 0)
                return new DoubleBlockReaction(PermissionFlag.MODIFY, pos, trace.getHitBlockPos(pos.world));

            return new DoubleBlockReaction(PermissionFlag.MODIFY, pos, trace.getHitBlockPos(pos.world).add(face));
        }

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }
}
