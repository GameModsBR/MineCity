package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.ReactiveTileEntity;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface BlockReactor
{
    @NotNull
    Optional<ReactiveBlockType> getBlockType(BlockTypeData block);

    @NotNull
    Optional<ReactiveBlockState> getBlockState(BlockStateData blockState);


    @NotNull
    Optional<ReactiveTileEntity> getTileEntity(TileEntityData tileEntity);

    @NotNull
    <T extends Comparable<T>> Optional<ReactiveBlockTrait<T>> getBlockTrait(BlockTraitData<T> blockTrait);
}
