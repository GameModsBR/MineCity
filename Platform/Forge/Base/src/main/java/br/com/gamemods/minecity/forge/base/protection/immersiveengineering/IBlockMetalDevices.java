package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockMetalDevices extends IBlockOpenReactor
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        ITileEntity tile = player.getIWorld().getTileEntity(pos);
        if(stack != null && stack.isTool("IE_HAMMER"))
        {
            if(tile instanceof ImmersiveTileModifyOnHammer)
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        }

        if(tile instanceof ITileEntityConveyorSorter && !player.isSneaking())
            return new SingleBlockReaction(pos, PermissionFlag.OPEN);

        if(tile instanceof ITileEntitySampleDrill)
            return new SingleBlockReaction(pos, PermissionFlag.CLICK);

        return NoReaction.INSTANCE;
    }
}
