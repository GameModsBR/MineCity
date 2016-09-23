package br.com.gamemods.minecity.forge.mc_1_7_10.protection;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.protection.SnapshotHandler;
import net.minecraft.block.Block;
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
        {
            World world = snapshot.world;
            int x = snapshot.x;
            int y = snapshot.y;
            int z = snapshot.z;
            int meta = world.getBlockMetadata(x, y, z);
            int flag = snapshot.flag;
            Block oldBlock = snapshot.replacedBlock;
            Block newBlock = world.getBlock(x, y, z);
            //noinspection ConstantConditions
            if(newBlock != null && !(newBlock.hasTileEntity(meta)))
                newBlock.onBlockAdded(world, x, y, z);

            world.markAndNotifyBlock(x, y, z, null, oldBlock, newBlock, flag);
        });
    }
}
