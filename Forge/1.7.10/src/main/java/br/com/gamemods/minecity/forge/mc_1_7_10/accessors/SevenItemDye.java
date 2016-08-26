package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IItemDye;
import br.com.gamemods.minecity.forge.base.accessors.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import br.com.gamemods.minecity.forge.base.protection.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import br.com.gamemods.minecity.forge.base.protection.SingleBlockReaction;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenItemDyeTransformer;
import net.minecraft.init.Blocks;

@Referenced(at = SevenItemDyeTransformer.class)
public interface SevenItemDye extends IItemDye
{
    @Override
    default Reaction react(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos, Direction face)
    {
        if(stack.getMeta() != 3 || state.getIBlock() != Blocks.log)
            return NoReaction.INSTANCE;

        if((state.getStateId() & 3) != 3)
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(pos.add(face.getOpposite()), PermissionFlag.MODIFY);
    }
}
