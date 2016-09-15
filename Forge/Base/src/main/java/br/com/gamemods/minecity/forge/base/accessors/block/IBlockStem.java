package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IBlockStem extends IBlock
{
    default IItemStack getISeed(IState state, IWorldServer world, int x, int y, int z)
    {
        return getItemStack(getDefaultIState(), world, x, y, z);
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap, IItemStack hand, boolean offHand)
    {
        // Allow to use bone meal
        IItemStack stack = player.cmd.sender.getStackInHand(player.offHand);
        if(stack != null && stack.getItem() != getISeed(snap.getCurrentState(), snap.getIWorld(), snap.getX(), snap.getY(), snap.getZ()))
            return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.HARVEST);

        return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.MODIFY);
    }
}
