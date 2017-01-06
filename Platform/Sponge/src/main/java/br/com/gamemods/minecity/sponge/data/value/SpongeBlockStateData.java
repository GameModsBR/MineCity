package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.property.block.ReplaceableProperty;

import java.util.Optional;
import java.util.stream.Stream;

public class SpongeBlockStateData implements BlockStateData
{
    private final SpongeManipulator manipulator;
    private final BlockState blockState;

    public SpongeBlockStateData(SpongeManipulator manipulator, BlockState blockState)
    {
        this.manipulator = manipulator;
        this.blockState = blockState;
    }

    @Override
    public BlockState getBlockState()
    {
        return blockState;
    }

    @NotNull
    @Override
    public <V extends Comparable<V>> Optional<V> getTrait(BlockTraitData<V> traitData)
    {
        return blockState.getTraitValue(getBlockTrait(traitData));
    }

    private <V extends Comparable<V>> BlockTrait<V> getBlockTrait(BlockTraitData<V> traitData)
    {
        if(traitData instanceof SpongeBlockTraitData)
            return  ((SpongeBlockTraitData<V>) traitData).trait;
        else
            throw new UnsupportedOperationException("Unsupported traitData: "+traitData);
    }

    @Override
    public <T extends Comparable<T>> Optional<BlockStateData> withTrait(BlockTraitData<T> trait, T value)
    {
        BlockTrait<T> spongeTrait = getBlockTrait(trait);
        return blockState.withTrait(spongeTrait, value).map(manipulator.block::getBlockStateData);
    }

    @NotNull
    @Override
    public BlockTypeData getBlockTypeData()
    {
        return manipulator.block.getBlockTypeData(blockState.getType());
    }

    @Override
    public Stream<BlockTraitData<?>> blockTraitStream()
    {
        return blockState.getTraits().stream().map(manipulator.block::getBlockTraitData);
    }

    @Override
    public boolean isReplaceable()
    {
        return blockState.getProperty(ReplaceableProperty.class).map(ReplaceableProperty::getValue).orElse(false);
    }

    @Override
    public String toString()
    {
        return "SpongeBlockStateData{"+
                "blockState="+blockState+
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        SpongeBlockStateData that = (SpongeBlockStateData) o;

        return blockState.equals(that.blockState);
    }

    @Override
    public int hashCode()
    {
        return blockState.hashCode();
    }
}
