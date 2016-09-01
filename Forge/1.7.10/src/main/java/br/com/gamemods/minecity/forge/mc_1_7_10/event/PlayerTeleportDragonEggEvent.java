package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Collections;
import java.util.List;

@Cancelable
public class PlayerTeleportDragonEggEvent extends BlockEvent
{
    public final EntityPlayer player;
    public final List<BlockSnapshot> changes;
    public PlayerTeleportDragonEggEvent(EntityPlayer player, int x, int y, int z, World world, Block block, int blockMetadata, List<BlockSnapshot> changes)
    {
        super(x, y, z, world, block, blockMetadata);
        this.player = player;
        this.changes = Collections.unmodifiableList(changes);
    }
}
