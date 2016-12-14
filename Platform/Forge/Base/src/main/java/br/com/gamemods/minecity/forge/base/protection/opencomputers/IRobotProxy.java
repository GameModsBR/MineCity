package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ApproveReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IRobotProxy extends ISimpleBlock
{
    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        MineCityForge mod = player.getServer();
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(tile instanceof IAgent)
        {
            IAgent agent = (IAgent) tile;
            if(player.getEntityUUID().equals(agent.ownerUUID()))
            {
                IItem drop = getItemDropped(state, MineCity.RANDOM, 0);
                ApproveReaction reaction = new ApproveReaction(pos, PermissionFlag.MODIFY);
                reaction.addAllowListener((reaction1, permissible, flag, pos1, message) ->
                        mod.consumeItemsOrAddOwnerIf(agent.envEntityPos(mod), 2, 1, 2, null, player.identity(),
                                entity-> entity.getStack().getIItem() == drop
                        )
                );
                return reaction;
            }
        }

        return ISimpleBlock.super.reactBlockBreak(player, state, pos);
    }

    @Override
    default Reaction reactRightClickActivation(BlockPos pos, IState state, IEntityPlayerMP player,
                                               IItemStack stack, boolean offHand, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(tile instanceof IAgent)
        {
            IAgent agent = (IAgent) tile;
            if(player.getEntityUUID().equals(agent.ownerUUID()))
                return NoReaction.INSTANCE;
        }

        return ISimpleBlock.super.reactRightClickActivation(pos, state, player, stack, offHand, face);
    }
}
