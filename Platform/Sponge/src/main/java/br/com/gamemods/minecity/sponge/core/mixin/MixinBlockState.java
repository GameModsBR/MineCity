package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedBlockState;
import net.minecraft.block.state.BlockStateBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("ClassHasNoToStringMethod")
@Mixin(BlockStateBase.class)
public abstract class MixinBlockState implements MixedBlockState
{
    @Nullable
    private BlockStateData blockStateData;

    @NotNull
    @Override
    public BlockStateData getBlockStateData()
    {
        if(blockStateData != null)
            return blockStateData;

        return blockStateData = ReactiveLayer.getBlockManipulator().getBlockStateData(this).get();
    }
}
