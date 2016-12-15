package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

public interface OwnedDevice
{
    default void onBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap, IItemStack hand, boolean offHand)
    {
        ITileEntity tile = snap.getCurrentTileEntity();
        if(tile instanceof ITileOwned)
            ((ITileOwned) tile).setOwner(player.identity());
    }

    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof ITileOwned))
            return new SingleBlockReaction(pos, PermissionFlag.CLICK);

        ITileOwned owned = (ITileOwned) tile;
        PlayerID id = player.identity();
        if(owned.isOwner(id))
            return new SingleBlockReaction(pos, PermissionFlag.CLICK);

        FlagHolder fh = player.getServer().mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos);
        if(fh.owner().equals(id))
        {
            owned.setOwner(id);
            return NoReaction.INSTANCE;
        }

        // This call will update the name if necessary
        owned.hasAccess(id);
        return NoReaction.INSTANCE;
    }
}
