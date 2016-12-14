package br.com.gamemods.minecity.reactive;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TestBlockTypeData implements BlockTypeData
{
    @NotNull
    private Object block;

    @Nullable
    ReactiveBlockType reactive;

    public TestBlockTypeData(@NotNull Object block)
    {
        this.block = block;
    }

    @Override
    public Object getBlockType()
    {
        return block;
    }

    @NotNull
    @Override
    public Optional<ReactiveBlockType> getReactiveBlockType()
    {
        return Optional.ofNullable(reactive);
    }

    @Override
    public void setReactive(@Nullable ReactiveBlockType reactiveBlock)
    {
        System.out.println("Setting "+block+" to "+reactiveBlock);
        reactive = reactiveBlock;
    }

    @Nullable
    public ReactiveBlockType getReactive()
    {
        System.out.println("Getting "+block+" -- "+reactive);
        return reactive;
    }
}
