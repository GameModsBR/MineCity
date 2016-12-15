package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.BlockAndSidesReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

import java.util.EnumSet;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemInkwell extends IItem
{
    @Override
    default Reaction reactRightClickBlockFirstUse(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos, Direction face)
    {
        IBlock block = state.getIBlock();
        return new BlockAndSidesReaction(PermissionFlag.MODIFY, pos, EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST))
                .condition(other-> pos.world.getInstance(IWorldServer.class).getIBlock(other) == block);
    }
}
