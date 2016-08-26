package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.*;
import br.com.gamemods.minecity.forge.base.protection.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import br.com.gamemods.minecity.forge.base.protection.SingleBlockReaction;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostItemDyeTransformer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

@Referenced(at = FrostItemDyeTransformer.class)
public interface FrostItemDye extends IItemDye
{
    @Override
    default Reaction react(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos,
                           Direction face)
    {
        assert pos.world.instance != null;
        EnumDyeColor color = EnumDyeColor.byDyeDamage(stack.getMeta());
        if(color == EnumDyeColor.WHITE)
        {
            IBlock block = state.getIBlock();
            if(block instanceof BlockCrops)
                return new SingleBlockReaction(pos, PermissionFlag.HARVEST);
            else if(block instanceof BlockTallGrass &&
                    !(((IWorldServer)pos.world.instance).getIBlock(pos.x, pos.y-1, pos.z) instanceof BlockTallGrass))
                return new SingleBlockReaction(pos.add(Direction.UP), PermissionFlag.MODIFY);
            else
                return NoReaction.INSTANCE;
        }

        if(color != EnumDyeColor.BROWN || state.getIBlock() != Blocks.LOG)
            return NoReaction.INSTANCE;

        if(((IBlockState) state).getValue(BlockOldLog.VARIANT) != BlockPlanks.EnumType.JUNGLE)
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(pos.add(face.getOpposite()), PermissionFlag.MODIFY);
    }
}
