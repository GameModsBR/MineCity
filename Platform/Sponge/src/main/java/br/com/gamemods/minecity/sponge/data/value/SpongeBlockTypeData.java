package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockType;

import java.util.Optional;

public class SpongeBlockTypeData implements BlockTypeData
{
    private final SpongeManipulator manipulator;
    private final BlockType blockType;
    @Nullable
    private ReactiveBlockType reactive;

    public SpongeBlockTypeData(SpongeManipulator manipulator, BlockType blockType)
    {
        this.manipulator = manipulator;
        this.blockType = blockType;
    }

    @NotNull
    @Override
    public Optional<ReactiveBlockType> getReactiveBlockType()
    {
        if(reactive != null)
            return Optional.of(reactive);

        return BlockTypeData.super.getReactiveBlockType();
    }

    @Override
    public void setReactive(@Nullable ReactiveBlockType reactiveBlock)
    {
        reactive = reactiveBlock;
    }

    @Override
    public BlockType getBlockType()
    {
        return blockType;
    }

    @NotNull
    @Override
    public Optional<String> getBlockIdName()
    {
        return Optional.of(blockType.getName());
    }

    @NotNull
    @Override
    public Optional<Integer> getBlockId()
    {
        return Optional.empty();
    }
}
