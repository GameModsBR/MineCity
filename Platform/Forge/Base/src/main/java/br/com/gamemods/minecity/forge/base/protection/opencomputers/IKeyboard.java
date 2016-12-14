package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IKeyboard extends ISimpleBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof IRotatable))
            return NoReaction.INSTANCE;

        /*
        IRotatable rotatable = (IRotatable) tile;
        Direction facing = ForgeUtil.toDirection(rotatable.facing().ordinal()).getOpposite();
        BlockPos screenPos = pos.add(facing);
        IState screenState = world.getIState(screenPos);
        IBlock screen = screenState.getIBlock();
        if(!(screen instanceof IScreen))
        {
            if(facing == Direction.UP || facing == Direction.DOWN)
                facing = ForgeUtil.toDirection(rotatable.yaw().ordinal());
            else
                facing = Direction.UP;

            screenPos = pos.add(facing);
            screenState = world.getIState(screenPos);
            screen = screenState.getIBlock();
            if(!(screen instanceof IScreen))
            {
                if(facing != Direction.UP && facing != Direction.DOWN)
                {
                    facing = facing.getOpposite();
                    screenPos = pos.add(facing);
                    screenState = world.getIState(screenPos);
                    screen = screenState.getIBlock();
                    if(!(screen instanceof IScreen))
                        return NoReaction.INSTANCE;
                }
                else
                {
                    return NoReaction.INSTANCE;
                }
            }
        }
        */

        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.CLICK);
        reaction.onDenyCloseScreen(player);
        return reaction/*.combine(
                ((IScreen) screen).reactRightClickActivation(screenPos, screenState, player, stack, offHand, face)
        )*/;
    }
}
