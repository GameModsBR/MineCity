package br.com.gamemods.minecity.forge.mc_1_7_10.protection;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCitySeven;
import br.com.gamemods.minecity.protection.MovementListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidBase;

public interface SevenMovementListener extends MovementListener<Entity, MineCitySeven>
{
    default boolean isSafeToStep(MineCitySeven server, Entity entity, WorldDim dim, int x, int y, int z)
    {
        WorldServer world = server.world(dim);
        return world != null && isSafeToStep(entity, world.getBlock(x, y, z));
    }

    default boolean isSafeToStep(Entity entity, Block block)
    {
        return block.isOpaqueCube() || block instanceof BlockLiquid || block instanceof BlockFluidBase ||
                entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying;
    }
}
