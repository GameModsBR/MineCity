package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;

import java.util.Collections;
import java.util.List;

public interface SnapshotHandler
{
    void restore(List<IBlockSnapshot> snapshotList);
    void send(List<IBlockSnapshot> snapshotList);
    default void send(BlockPos pos)
    {
        send(Collections.singletonList(pos.world.getInstance(IWorldServer.class).getBlockSnapshot(pos)));
    }
}
