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
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import org.jetbrains.annotations.Nullable;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockMetalDevices2 extends IBlockOpenReactor
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, @Nullable IItemStack stack,
                                     boolean offHand, Direction face)
    {
        ITileEntity tile = player.getIWorld().getTileEntity(pos);
        if(tile instanceof IIColouredTile)
        {
            if(stack != null && ImmersiveHooks.getDye(stack.getStack()) >= 0)
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        }

        if(!player.isSneaking() && tile instanceof ITileEntityBreakerSwitch && !(tile instanceof ITileEntityRedstoneBreaker))
        {
            if(stack != null && stack.isTool("IE_HAMMER"))
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

            return new SingleBlockReaction(pos, PermissionFlag.CLICK);
        }

        if(stack != null && stack.isTool("IE_HAMMER") && tile instanceof ImmersiveTileModifyOnHammer)
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        if(tile instanceof ITileEntityEnergyMeter)
            return new SingleBlockReaction(pos, PermissionFlag.CLICK);

        if(tile instanceof ITileEntityChargingStation || tile instanceof ITileEntityWoodenBarrel)
            return new SingleBlockReaction(pos, PermissionFlag.OPEN);

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }
}
