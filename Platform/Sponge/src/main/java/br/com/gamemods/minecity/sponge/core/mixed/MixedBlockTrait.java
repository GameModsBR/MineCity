package br.com.gamemods.minecity.sponge.core.mixed;

import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.supplier.SupplierReactiveBlockTrait;

public interface MixedBlockTrait<T extends Comparable<T>> extends SupplierBlockTraitData<T>, SupplierReactiveBlockTrait<T>
{
}
