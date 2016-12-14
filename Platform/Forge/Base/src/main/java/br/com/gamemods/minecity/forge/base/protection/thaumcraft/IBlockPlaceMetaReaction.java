package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

public interface IBlockPlaceMetaReaction extends IBlock
{
    default Reaction reactPlace(BlockPos pos, int meta)
    {
        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        Reaction reaction = reactPlace(pos, stack.getMeta());
        if(reaction != null)
            return reaction;

        return IBlock.super.reactPrePlace(who, stack, pos);
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap, IItemStack hand,
                                     boolean offHand)
    {
        Reaction r = reactPlace(snap.getPosition(player.getServer()), snap.getCurrentState().getIntValueOrMeta("metadata"));
        if(r != null)
            return r;

        return IBlock.super.reactBlockPlace(player, snap, hand, offHand);
    }
}
