package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Collections;
import java.util.List;

@Cancelable
public class BlockGrowEvent extends BlockEvent
{
    public final Object source;
    public final List<BlockSnapshot> changes;
    public BlockGrowEvent(World world, int x, int y, int z, Object source, List<BlockSnapshot> changes)
    {
        super(x, y, z, world, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
        this.changes = Collections.unmodifiableList(changes);
        this.source = source;
    }
}
