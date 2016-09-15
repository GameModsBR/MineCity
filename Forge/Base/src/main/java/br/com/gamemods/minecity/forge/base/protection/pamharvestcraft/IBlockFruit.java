package br.com.gamemods.minecity.forge.base.protection.pamharvestcraft;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.DenyReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockFruit extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        if(state.getIntValueOrMeta("age") != 2)
            return NoReaction.INSTANCE;

        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.HARVEST);
        reaction.addAllowListener((reaction1, permissible, flag, pos1, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(pos.toEntity(), 2, 1, 1, null, player.identity(), item ->
                        item.getStack().getIItem() == getItemDropped(state, MineCity.RANDOM, 0)
                )
        );
        return reaction;
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        if(state.getIntValueOrMeta("age") != 2)
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        if(player.cmd.sender.isCreative())
            return new DenyReaction(new Message(
                    "action.harvest-on-creative",
                    "You can't harvest plants on creative mode, that would just reset the growth state without any drop."
            ));

        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.HARVEST);
        reaction.addAllowListener((reaction1, permissible, flag, pos1, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(pos.toEntity(), 2, 1, 1, null, player.identity(), item ->
                        item.getStack().getIItem() == getItemDropped(state, MineCity.RANDOM, 0)
                )
        );
        return reaction;
    }
}
