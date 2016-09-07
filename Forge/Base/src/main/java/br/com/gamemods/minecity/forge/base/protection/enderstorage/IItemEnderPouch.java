package br.com.gamemods.minecity.forge.base.protection.enderstorage;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemEnderPouch extends IItem
{
    @Override
    default Reaction reactRightClickBlockFirstUse(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        if(!player.isSneaking())
            return NoReaction.INSTANCE;

        ITileEntity tile = player.getIWorld().getTileEntity(pos);
        if(!(tile instanceof ITileFrequencyOwner))
            return NoReaction.INSTANCE;

        ITileFrequencyOwner block = (ITileFrequencyOwner) tile;
        if(block.isOwner(player.identity()))
            return NoReaction.INSTANCE;

        if(block.isPublic())
            return new SingleBlockReaction(pos, PermissionFlag.OPEN);

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }
}
