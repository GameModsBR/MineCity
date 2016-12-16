package br.com.gamemods.minecity.reactive.reactor.builtin;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.ReactiveTileEntity;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.reactive.game.block.supplier.SupplierReactiveBlockState;
import br.com.gamemods.minecity.reactive.game.block.supplier.SupplierReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.supplier.SupplierReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.supplier.SupplierReactiveTileEntity;
import br.com.gamemods.minecity.reactive.reactor.BlockReactor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BuiltinBlockReactor implements BlockReactor
{
    @NotNull
    @Override
    public Optional<ReactiveBlockType> getReactiveBlockType(BlockTypeData data)
    {
        Object blockType = data.getBlockType();
        if(blockType instanceof SupplierReactiveBlockType)
            return ((SupplierReactiveBlockType) blockType).getReactiveBlockType().map(ReactiveBlockType.class::cast);
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<ReactiveBlockState> getReactiveBlockState(BlockStateData data)
    {
        Object blockState = data.getBlockState();
        if(blockState instanceof SupplierReactiveBlockState)
            return ((SupplierReactiveBlockState) blockState).getReactiveBlockState();
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<ReactiveTileEntity> getReactiveTileEntity(TileEntityData data)
    {
        Object tileEntity = data.getTileEntity();
        if(tileEntity instanceof SupplierReactiveTileEntity)
            return ((SupplierReactiveTileEntity) tileEntity).getReactiveTileEntity();
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T extends Comparable<T>> Optional<ReactiveBlockTrait<T>> getReactiveBlockTrait(BlockTraitData<T> data)
    {
        Object blockTrait = data.getBlockTrait();
        if(blockTrait instanceof SupplierReactiveBlockTrait)
            return ((SupplierReactiveBlockTrait<T>) blockTrait).getReactiveBlockTrait();
        return Optional.empty();
    }
}
