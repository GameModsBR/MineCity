package br.com.gamemods.minecity.forge.mc_1_7_10.protection;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.protection.SnapshotHandler;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;

import java.util.HashSet;
import java.util.List;

public class SevenSnapshotHandler implements SnapshotHandler
{
    @SuppressWarnings("unchecked")
    public void restore(List<IBlockSnapshot> changes)
    {
        HashSet<Point> restored = new HashSet<>();
        for(BlockSnapshot snapshot : (List<BlockSnapshot>) (List) changes)
        {
            Point snapPos = new Point(snapshot.x, snapshot.y, snapshot.z);
            if(restored.contains(snapPos))
                continue;

            World world = snapshot.world;
            world.restoringBlockSnapshots = true;
            snapshot.restore(true, false);
            world.restoringBlockSnapshots = false;
            restored.add(snapPos);
        }
    }

    public void send(List<IBlockSnapshot> changes)
    {
        changes.stream().map(BlockSnapshot.class::cast).forEachOrdered(snapshot ->
                snapshot.world.markBlockForUpdate(snapshot.x, snapshot.y, snapshot.z)
        );
    }
}
