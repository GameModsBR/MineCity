package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemGolemBell extends IItem
{
    @Override
    default Reaction reactRightClickBlockFirstUse(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                                  IState state, BlockPos pos, Direction face)
    {
        IRayTraceResult trace = rayTrace(pos.world.getInstance(IWorldServer.class), player, true);
        if(trace == null || trace.getHitType() != 1)
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        return new SingleBlockReaction(trace.getHitBlockPos(pos.world), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactInteractEntity(IEntityPlayerMP player, IEntity target, IItemStack stack,
                                         boolean offHand)
    {
        if(target instanceof IEntityGolemBase)
            return new SingleBlockReaction(target.getBlockPos(player.getServer()), PermissionFlag.MODIFY);

        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactPlayerAttackDirect(IEntityPlayerMP player, IEntity target, IItemStack stack,
                                             boolean offHand)
    {
        if(target instanceof IEntityGolemBase)
            return new SingleBlockReaction(target.getBlockPos(player.getServer()), PermissionFlag.MODIFY);

        return NoReaction.INSTANCE;
    }
}
