package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.Collections;
import java.util.List;

@Cancelable
public class PlayerTeleportDragonEggEvent extends BlockEvent
{
    public final EntityPlayer player;
    public final List<BlockSnapshot> changes;
    public PlayerTeleportDragonEggEvent(EntityPlayer player, World world, BlockPos pos, IBlockState state, List<BlockSnapshot> changes)
    {
        super(world, pos, state);
        this.player = player;
        this.changes = Collections.unmodifiableList(changes);
    }
}
