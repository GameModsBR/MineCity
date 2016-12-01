package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

import java.util.Arrays;
import java.util.Optional;

@Referenced(at = ModInterfacesTransformer.class)
public interface IRobotAfterimage extends IRobotProxy
{
    @Override
    default Reaction reactRightClickActivation(BlockPos pos, IState state, IEntityPlayerMP player,
                                               IItemStack stack, boolean offHand, Direction face)
    {
        Optional<IRobotTile> tile = findMovingRobot(pos);
        if(tile.isPresent() && player.getUniqueID().equals(tile.get().ownerUUID()))
            return NoReaction.INSTANCE;

        return IRobotProxy.super.reactRightClickActivation(pos, state, player, stack, offHand, face);
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        Optional<IRobotTile> tile = findMovingRobot(pos);
        if(tile.isPresent() && player.getEntityUUID().equals(tile.get().ownerUUID()))
        {
            IRobotTile robot = tile.get();
            BlockPos robotPos = new BlockPos(pos, (int)robot.xPosition(), (int)robot.yPosition(), (int)robot.zPosition());
            IWorldServer world = pos.world.getInstance(IWorldServer.class);
            return IRobotProxy.super.reactBlockBreak(player, world.getIState(robotPos), robotPos);
        }

        return IRobotProxy.super.reactBlockBreak(player, state, pos);
    }

    static Optional<IRobotTile> findMovingRobot(BlockPos pos)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        return Arrays.stream(Direction.values())
                .map(pos::add).filter(world::isBlockLoaded)
                .map(world::getTileEntity).filter(IRobotProxyTile.class::isInstance).map(IRobotProxyTile.class::cast)
                .map(IRobotProxyTile::robotTile).map(IRobotTile.class::cast)
                .filter(tile-> !tile.moveFrom().isEmpty())
                .findFirst();
    }
}
