package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Cancelable
public class PistonMoveEvent extends BlockEvent
{
    public final EnumFacing direction;
    public final boolean extend;
    public final List<IBlockSnapshot> changes;
    public final Object movedBy;

    @SuppressWarnings("unchecked")
    public PistonMoveEvent(World world, BlockPos pos, IBlockState state, EnumFacing dir, boolean extend, List<BlockSnapshot> changes, Object movedBy)
    {
        super(world, pos, state);
        this.direction = dir;
        this.extend = extend;
        this.changes = Collections.unmodifiableList(new ArrayList(changes));
        this.movedBy = movedBy;
    }
}
