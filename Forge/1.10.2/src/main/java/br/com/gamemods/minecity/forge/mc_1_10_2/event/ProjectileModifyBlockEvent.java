package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ProjectileModifyBlockEvent extends BlockEvent
{
    public final Entity projectile;

    public ProjectileModifyBlockEvent(Entity projectile, World world, BlockPos pos, IBlockState state)
    {
        super(world, pos, state);
        this.projectile = projectile;
    }
}
