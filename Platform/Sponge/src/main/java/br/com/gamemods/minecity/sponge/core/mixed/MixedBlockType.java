package br.com.gamemods.minecity.sponge.core.mixed;

import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTypeData;
import org.jetbrains.annotations.Nullable;

public interface MixedBlockType extends SupplierBlockTypeData
{
    void setBlockTypeData(@Nullable BlockTypeData blockTypeData);
}
