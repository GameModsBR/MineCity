package br.com.gamemods.minecity.forge.base.protection.universalcoinsserver;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockOwned extends IBlockNoReactionExtendsOpen
{
    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        ITileEntity tile = pos.world.getInstance(IWorldServer.class).getTileEntity(pos);
        if(tile instanceof IPlayerOwned)
        {
            UUID owner = ((IPlayerOwned) tile).getOwnerId();
            if(player.getEntityUUID().equals(owner))
                return NoReaction.INSTANCE;
        }

        return IBlockNoReactionExtendsOpen.super.reactBlockBreak(player, state, pos);
    }
}
