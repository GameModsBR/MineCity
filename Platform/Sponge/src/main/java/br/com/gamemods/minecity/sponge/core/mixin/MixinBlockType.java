package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedBlockType;
import br.com.gamemods.minecity.sponge.core.mixed.Reactive;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(Block.class)
public abstract class MixinBlockType implements MixedBlockType
{
    @Nullable
    private BlockTypeData blockTypeData;
    private Reactive<ReactiveBlockType> reactiveBlockType = new Reactive<>(()->
            ReactiveLayer.getBlockReactor().getBlockType(getBlockTypeData())
    );

    @NotNull
    @Override
    public BlockTypeData getBlockTypeData()
    {
        if(blockTypeData != null)
            return blockTypeData;

        return blockTypeData = ReactiveLayer.getBlockManipulator().getBlockTypeData(this).get();
    }

    @NotNull
    @Override
    public Optional<ReactiveBlockType> getReactiveBlockType()
    {
        return reactiveBlockType.get();
    }
}
