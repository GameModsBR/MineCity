package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockState;
import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Cancelable
public class PistonMoveEvent extends BlockEvent
{
    public final Direction direction;
    public final boolean extend;
    public final List<IBlockSnapshot> changes;
    public final Object movedBy;
    public final SevenBlockState state;

    @SuppressWarnings("unchecked")
    public PistonMoveEvent(World world, int x, int y, int z, SevenBlockState state, int dir, boolean extend, List<BlockSnapshot> changes, Object movedBy)
    {
        super(x, y, z, world, (Block)state.block, state.meta);
        this.state = state;
        this.direction = ForgeUtil.toDirection(dir);
        this.extend = extend;
        this.changes = Collections.unmodifiableList(new ArrayList(changes));
        this.movedBy = movedBy;
    }
}
