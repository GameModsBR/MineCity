package br.com.gamemods.minecity.sponge.data.manipulator.reactive;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.ReactiveTileEntity;
import br.com.gamemods.minecity.reactive.game.block.data.*;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTypeData;
import br.com.gamemods.minecity.reactive.reactor.BlockReactor;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.MineCityKeys;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.TileEntityDataManipulator;
import br.com.gamemods.minecity.sponge.data.value.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataTransactionResult;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator.handleSupplier;

public class SpongeBlockManipulator implements BlockManipulator, BlockReactor
{
    private final SpongeManipulator manipulator;
    private final ThreadLocal<BlockType> handlingBlockType = new ThreadLocal<>();
    private final ThreadLocal<BlockTrait> handlingBlockTrait = new ThreadLocal<>();
    private final Map<String, BlockTrait<?>> traitMap;

    public SpongeBlockManipulator(SpongeManipulator manipulator)
    {
        this.manipulator = manipulator;
        traitMap = Sponge.getGame().getRegistry().getAllOf(BlockType.class).stream()
                .map(BlockType::getTraits).flatMap(Collection::stream).distinct()
                .collect(Collectors.toMap(trait-> {
                    try
                    {
                        return trait.getId();
                    }
                    catch(NullPointerException e)
                    {
                        return "NOID:"+trait.getName();
                    }
                }, Function.identity()));
    }

    @NotNull
    @Override
    public Collection<BlockTypeData> findBlockTypes(@NotNull Class<?> filter)
    {
        return Sponge.getGame().getRegistry().getAllOf(BlockType.class).stream().filter(filter::isInstance)
                .map(this::getBlockTypeData).collect(Collectors.toList());
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
    public Optional<ReactiveBlockType> getReactiveBlockType(BlockTypeData block)
    {
        if(block instanceof SpongeBlockTypeData)
            return block.getReactiveBlockType();

        return Optional.empty();
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
    public Optional<ReactiveBlockState> getReactiveBlockState(BlockStateData blockState)
    {
        return Optional.empty();
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
    public Optional<ReactiveTileEntity> getReactiveTileEntity(TileEntityData tileEntity)
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<BlockTraitData<?>> getBlockTraitData(@NotNull Object blockTrait)
    {
        if(blockTrait instanceof CharSequence)
        {
            blockTrait = traitMap.get(blockTrait.toString());
            if(blockTrait instanceof SupplierBlockTraitData)
                return Optional.of(((SupplierBlockTraitData) blockTrait).getBlockTraitData());

            manipulator.sponge.logger.warn("Block trait not found: "+blockTrait);
            manipulator.sponge.logger.warn(traitMap.toString());
        }

        if(!(blockTrait instanceof BlockTrait))
            return Optional.empty();

        return Optional.of(getBlockTraitData((BlockTrait<?>) blockTrait));
    }

    public <T extends Comparable<T>> BlockTrait<T> getBlockTrait(BlockTraitData<T> traitData)
    {
        if(traitData instanceof SpongeBlockTraitData)
            return ((SpongeBlockTraitData<T>) traitData).trait;

        throw new UnsupportedOperationException("Unsupported trait: "+traitData.getClass()+": "+traitData);
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> BlockTraitData<T> getBlockTraitData(BlockTrait<T> blockTrait)
    {
        return (BlockTraitData<T>) handleSupplier(handlingBlockTrait, blockTrait, SupplierBlockTraitData.class,
                SupplierBlockTraitData::getBlockTraitData,
                ()-> new SpongeBlockTraitData(manipulator, blockTrait)
        );
    }

    @NotNull
    @Override
    public <T extends Comparable<T>> Optional<ReactiveBlockTrait<T>> getReactiveBlockTrait(BlockTraitData<T> blockTrait)
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<BlockSnapshotData> getBlockSnapshotData(@NotNull Object blockSnapshot)
    {
        if(!(blockSnapshot instanceof BlockSnapshot))
            return Optional.empty();

        return Optional.of(getBlockSnapshotData((BlockSnapshot) blockSnapshot));
    }

    @NotNull
    public SpongeBlockSnapshotData getBlockSnapshotData(@NotNull BlockSnapshot blockSnapshot)
    {
        return new SpongeBlockSnapshotData(manipulator, blockSnapshot);
    }

    @Override
    public String toString()
    {
        return "SpongeBlockManipulator{"+
                "manipulator="+manipulator+
                '}';
    }
}
