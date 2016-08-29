package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

@Cancelable
public class ProjectileModifyBlockEvent extends BlockEvent
{
    public final Entity projectile;

    public ProjectileModifyBlockEvent(Entity projectile, World world, int x, int y, int z, Block block)
    {
        super(x, y, z, world, block, world.getBlockMetadata(x, y, z));
        this.projectile = projectile;
    }
}
