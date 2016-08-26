package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.BlockRailBase;

public interface IItemMinecart extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        if(state.getIBlock() instanceof BlockRailBase)
        {
            // TODO Allow the player to get the vehicle back
            if(getUnlocalizedName().equals("minecart"))
                return new SingleBlockReaction(pos, PermissionFlag.VEHICLE);
            else
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        }

        return NoReaction.INSTANCE;
    }
}
