package br.com.gamemods.minecity.sponge.data.manipulator.reactive;

import br.com.gamemods.minecity.reactive.game.block.data.*;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.MineCityKeys;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.TileEntityDataManipulator;
import br.com.gamemods.minecity.sponge.data.value.SpongeBlockStateData;
import br.com.gamemods.minecity.sponge.data.value.SpongeBlockTraitData;
import br.com.gamemods.minecity.sponge.data.value.SpongeBlockTypeData;
import br.com.gamemods.minecity.sponge.data.value.SpongeTileEntityData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataTransactionResult;

import java.util.Optional;

public class SpongeBlockManipulator implements BlockManipulator
{
    private final SpongeManipulator manipulator;

    public SpongeBlockManipulator(SpongeManipulator manipulator)
    {
        this.manipulator = manipulator;
    }

    @NotNull
    @Override
    public Optional<BlockTypeData> getBlockType(@NotNull Object block)
    {
        if(!(block instanceof BlockType))
            return Optional.empty();

        return Optional.of(getBlockType((BlockType) block));
    }

    public BlockTypeData getBlockType(@NotNull BlockType blockType)
    {
        return new SpongeBlockTypeData(manipulator, blockType);
    }

    @NotNull
    @Override
    public Optional<BlockStateData> getBlockState(@NotNull Object blockState)
    {
        if(!(blockState instanceof BlockState))
            return Optional.empty();

        return Optional.of(getBlockState((BlockState) blockState));
    }

    public BlockStateData getBlockState(BlockState blockState)
    {
        return new SpongeBlockStateData(manipulator, blockState);
    }

    @NotNull
    @Override
    public Optional<TileEntityData> getTileEntity(@NotNull Object tileEntity)
    {
        if(!(tileEntity instanceof TileEntityData))
            return Optional.empty();

        return Optional.of(getTileEntity((TileEntity) tileEntity));
    }

    public TileEntityData getTileEntity(TileEntity tileEntity)
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
    public Optional<BlockTraitData<?>> getBlockTrait(@NotNull Object blockTrait)
    {
        if(!(blockTrait instanceof BlockTrait))
            return Optional.empty();

        return Optional.of(getBlockTrait((BlockTrait) blockTrait));
    }

    public <T extends Comparable<T>> BlockTrait<T> getBlockTrait(BlockTraitData<T> traitData)
    {
        if(traitData instanceof SpongeBlockTraitData)
            return ((SpongeBlockTraitData<T>) traitData).trait;

        throw new UnsupportedOperationException("Unsupported trait: "+traitData.getClass()+": "+traitData);
    }

    public <T extends Comparable<T>> BlockTraitData<T> getBlockTrait(BlockTrait<T> blockTrait)
    {
        return new SpongeBlockTraitData<>(manipulator, blockTrait);
    }
}
