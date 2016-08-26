package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IItemDye;
import br.com.gamemods.minecity.forge.base.accessors.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import br.com.gamemods.minecity.forge.base.protection.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import br.com.gamemods.minecity.forge.base.protection.SingleBlockReaction;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public interface FrostItemDye extends IItemDye
{
    @Override
    default Reaction react(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos,
                           Direction face)
    {
        if(state.getIBlock() != Blocks.LOG || EnumDyeColor.byDyeDamage(stack.getMeta()) != EnumDyeColor.BROWN)
            return NoReaction.INSTANCE;

        if(((IBlockState) state).getValue(BlockOldLog.VARIANT) != BlockPlanks.EnumType.JUNGLE)
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(pos.add(face.getOpposite()), PermissionFlag.MODIFY);
    }
}
