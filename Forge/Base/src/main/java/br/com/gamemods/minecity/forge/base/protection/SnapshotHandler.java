package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;

import java.util.List;

public interface SnapshotHandler
{
    void restore(List<IBlockSnapshot> snapshotList);
    void send(List<IBlockSnapshot> snapshotList);
}
