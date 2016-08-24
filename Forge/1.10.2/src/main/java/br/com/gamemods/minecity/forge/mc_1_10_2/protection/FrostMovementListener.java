package br.com.gamemods.minecity.forge.mc_1_10_2.protection;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.protection.MovementListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidBase;

public interface FrostMovementListener extends MovementListener<Entity, MineCityFrost>
{
    default boolean isSafeToStep(MineCityFrost server, Entity entity, WorldDim dim, int x, int y, int z)
    {
        WorldServer world = server.world(dim);
        return world != null && isSafeToStep(entity, world.getBlockState(new BlockPos(x, y, z)));
    }

    default boolean isSafeToStep(Entity entity, IBlockState state)
    {
        if(state.isOpaqueCube())
            return true;

        Block block = state.getBlock();
        return block instanceof BlockLiquid || block instanceof BlockFluidBase ||
                entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isElytraFlying() ||
                entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying;

    }
}
