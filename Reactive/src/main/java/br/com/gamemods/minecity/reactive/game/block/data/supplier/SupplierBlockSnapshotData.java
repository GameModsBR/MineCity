package br.com.gamemods.minecity.reactive.game.block.data.supplier;

import br.com.gamemods.minecity.reactive.game.block.data.BlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import org.jetbrains.annotations.NotNull;

public interface SupplierBlockSnapshotData extends SupplierBlockStateData
{
    @NotNull
    BlockSnapshotData getBlockSnapshotData();

    @NotNull
    @Override
    default BlockStateData getBlockStateData()
    {
        return getBlockSnapshotData().getBlockStateData();
    }
}
