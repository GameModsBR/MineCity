package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedBlockState;
import br.com.gamemods.minecity.sponge.core.mixed.Reactive;
import net.minecraft.block.state.BlockStateBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(BlockStateBase.class)
public abstract class MixinBlockState implements MixedBlockState
{
    @Nullable
    private BlockStateData blockStateData;
    private Reactive<? extends ReactiveBlockState> reactiveBlockState = new Reactive<>(()->
            ReactiveLayer.getBlockReactor().getBlockState(getBlockStateData())
    );

    @NotNull
    @Override
    public BlockStateData getBlockStateData()
    {
        if(blockStateData != null)
            return blockStateData;

        return blockStateData = ReactiveLayer.getBlockManipulator().getBlockState(this).get();
    }

    @NotNull
    @Override
    public Optional<? extends ReactiveBlockState> getReactiveBlockState()
    {
        return reactiveBlockState.get();
    }
}
