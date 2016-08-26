package br.com.gamemods.minecity.forge.mc_1_10_2;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;

public class MineCityFrost extends MineCityForge
{
    public BlockPos block(World world, Vec3i pos)
    {
        return new BlockPos(world(world), pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos block(BlockPos base, Vec3i pos)
    {
        return new BlockPos(base, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockPos block(BlockSnapshot snapshot)
    {
        return null;
    }
}
