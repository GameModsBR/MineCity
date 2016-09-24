package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

import java.util.concurrent.atomic.AtomicBoolean;

@Referenced(at = ModInterfacesTransformer.class)
public interface Wandable
{
    default Reaction reactWandRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos,
                                         Direction face, AtomicBoolean tile)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY).onDenyUpdateInventory();
    }

    default Reaction reactWandRightClick(IWorldServer world, IItemStack stack, IEntityPlayerMP player, BlockPos hit,
                                         AtomicBoolean finalize)
    {
        return new SingleBlockReaction(hit, PermissionFlag.MODIFY);
    }

    default Reaction onUsingWandTick(IItemStack stack, IEntityPlayerMP player, int count)
    {
        return NoReaction.INSTANCE;
    }
}
