package br.com.gamemods.minecity.forge.mc_1_10_2.protection;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.protection.SnapshotHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;

import java.util.HashSet;
import java.util.List;

public class FrostSnapshotHandler implements SnapshotHandler
{
    @SuppressWarnings("unchecked")
    public void restore(List<IBlockSnapshot> changes)
    {
        HashSet<BlockPos> restored = new HashSet<>();
        for(BlockSnapshot snapshot : (List<BlockSnapshot>) (List) changes)
        {
            BlockPos snapPos = snapshot.getPos();
            if(restored.contains(snapPos))
                continue;

            World world = snapshot.getWorld();
            world.restoringBlockSnapshots = true;
            snapshot.restore(true, false);
            world.restoringBlockSnapshots = false;
            restored.add(snapPos);
        }
    }

    @SuppressWarnings("unchecked")
    public void send(List<IBlockSnapshot> changes)
    {
        HashSet<BlockPos> notified = new HashSet<>();
        for(BlockSnapshot snapshot : (List<BlockSnapshot>) (List) changes)
        {
            BlockPos snapPos = snapshot.getPos();
            if(notified.contains(snapPos))
                continue;

            snapshot.getWorld().notifyBlockUpdate(snapPos, snapshot.getReplacedBlock(), snapshot.getCurrentBlock(), snapshot.getFlag());
            notified.add(snapPos);
        }
    }
}
