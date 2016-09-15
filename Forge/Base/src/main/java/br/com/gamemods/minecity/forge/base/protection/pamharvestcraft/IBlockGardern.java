package br.com.gamemods.minecity.forge.base.protection.pamharvestcraft;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemBlock;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockGardern extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        reaction.addAllowListener((reaction1, permissible, flag, p, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(p.toEntity(), 2, 1, 2, null, player.identity(), item->
                        item.getStack().getIItem() instanceof IItemBlock &&
                                this == ((IItemBlock) item.getStack().getIItem()).getIBlock()
                )
        );
        return reaction;
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        reaction.addAllowListener((reaction1, permissible, flag, p, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(p.toEntity(), 2, 1, 2, null, player.identity(), item ->
                        item.getStack().getIItem() == getItemDropped(getDefaultIState(), MineCity.RANDOM, 0)
                )
        );
        return reaction;
    }
}
