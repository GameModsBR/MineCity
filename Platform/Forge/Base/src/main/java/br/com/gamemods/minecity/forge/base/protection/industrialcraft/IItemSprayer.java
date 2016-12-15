package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.RevertDeniedReaction;
import br.com.gamemods.minecity.reactive.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

import java.util.Collection;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemSprayer extends IItemIC2
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        return new DoubleBlockReaction(PermissionFlag.MODIFY, pos, pos.add(face));
    }

    @Override
    default Reaction reactBlockMultiPlace(IEntityPlayerMP entity, IItemStack hand, boolean offHand,
                                          BlockPos blockPos, Collection<IBlockSnapshot> snapshots)
    {
        return new RevertDeniedReaction(entity.getServer(), snapshots, PermissionFlag.MODIFY);
    }

    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return false;
    }
}
