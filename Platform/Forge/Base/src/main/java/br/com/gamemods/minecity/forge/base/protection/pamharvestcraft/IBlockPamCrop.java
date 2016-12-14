package br.com.gamemods.minecity.forge.base.protection.pamharvestcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockCrops;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockPamCrop extends IBlockCrops
{
    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        int age = state.getIntValueOrMeta("age");
        if(!isHarvestAge(age))
            return NoReaction.INSTANCE;

        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.HARVEST);
        reaction.addAllowListener((r, permissible, flag, p, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(p.precise(), 2, 1, 2, null, player.identity(), item->
                    item.getStack().getIItem().isHarvest(item.getStack())
            )
        );
        return reaction;
    }
}
