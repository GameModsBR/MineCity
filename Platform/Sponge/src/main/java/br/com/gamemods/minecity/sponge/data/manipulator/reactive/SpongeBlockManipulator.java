package br.com.gamemods.minecity.sponge.data.manipulator.reactive;

import br.com.gamemods.minecity.reactive.game.block.data.*;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTypeData;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.MineCityKeys;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.TileEntityDataManipulator;
import br.com.gamemods.minecity.sponge.data.value.SpongeBlockStateData;
import br.com.gamemods.minecity.sponge.data.value.SpongeBlockTraitData;
import br.com.gamemods.minecity.sponge.data.value.SpongeBlockTypeData;
import br.com.gamemods.minecity.sponge.data.value.SpongeTileEntityData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataTransactionResult;

import java.util.Optional;

import static br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator.handleSupplier;

public class SpongeBlockManipulator implements BlockManipulator
{
    private final SpongeManipulator manipulator;
    private final ThreadLocal<BlockType> handlingBlockType = new ThreadLocal<>();

    public SpongeBlockManipulator(SpongeManipulator manipulator)
    {
        this.manipulator = manipulator;
    }

    @NotNull
    @Override
    public Optional<BlockTypeData> getBlockTypeData(@NotNull Object block)
    {
        if(block instanceof CharSequence)
        {
            block = Sponge.getGame().getRegistry().getType(BlockType.class, block.toString()).orElse(null);
            if(block instanceof SupplierBlockTypeData)
                return Optional.of(((SupplierBlockTypeData) block).getBlockTypeData());
        }

        if(!(block instanceof BlockType))
            return Optional.empty();

        return Optional.of(getBlockTypeData((BlockType) block));
    }

    public BlockTypeData getBlockTypeData(@NotNull BlockType blockType)
    {
        return handleSupplier(handlingBlockType, blockType, SupplierBlockTypeData.class,
                SupplierBlockTypeData::getBlockTypeData,
                ()-> new SpongeBlockTypeData(manipulator, blockType)
        );
    }

    @NotNull
    @Override
    public Optional<BlockStateData> getBlockStateData(@NotNull Object blockState)
    {
        if(!(blockState instanceof BlockState))
            return Optional.empty();

        return Optional.of(getBlockStateData((BlockState) blockState));
    }

    public BlockStateData getBlockStateData(BlockState blockState)
    {
        return new SpongeBlockStateData(manipulator, blockState);
    }

    @NotNull
    @Override
    public Optional<TileEntityData> getTileEntityData(@NotNull Object tileEntity)
    {
        if(!(tileEntity instanceof TileEntityData))
            return Optional.empty();

        return Optional.of(getTileEntityData((TileEntity) tileEntity));
    }

    public TileEntityData getTileEntityData(TileEntity tileEntity)
    {
        Optional<TileEntityData> opt = tileEntity.get(MineCityKeys.TILE_ENTITY_DATA);
        if(opt.isPresent())
            return opt.get();

        SpongeTileEntityData data = new SpongeTileEntityData(manipulator, tileEntity);
        DataTransactionResult result = tileEntity.offer(new TileEntityDataManipulator(data));
        if(!result.isSuccessful())
            manipulator.sponge.logger.error("Failed to apply the tile entity data manipulator to the tile "+tileEntity);

        return data;
    }

    @NotNull
    @Override
    public Optional<BlockTraitData<?>> getBlockTraitData(@NotNull Object blockTrait)
    {
        if(!(blockTrait instanceof BlockTrait))
            return Optional.empty();

        return Optional.of(getBlockTraitData((BlockTrait<?>) blockTrait));
    }

    public <T extends Comparable<T>> BlockTrait<T> getBlockTraitData(BlockTraitData<T> traitData)
    {
        if(traitData instanceof SpongeBlockTraitData)
            return ((SpongeBlockTraitData<T>) traitData).trait;

        throw new UnsupportedOperationException("Unsupported trait: "+traitData.getClass()+": "+traitData);
    }

    public <T extends Comparable<T>> BlockTraitData<T> getBlockTraitData(BlockTrait<T> blockTrait)
    {
        return new SpongeBlockTraitData<>(manipulator, blockTrait);
    }
}
