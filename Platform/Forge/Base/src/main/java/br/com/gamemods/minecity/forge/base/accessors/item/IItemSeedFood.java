package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ForgeSingleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ForgeInterfaceTransformer.class)
@Referenced(at = ModInterfacesTransformer.class)
public interface IItemSeedFood extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        ForgeSingleBlockReaction react = new ForgeSingleBlockReaction(pos.add(face.getOpposite()), PermissionFlag.MODIFY);
        react.onDenyUpdateInventory();
        return react;
    }

    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return true;
    }
}
