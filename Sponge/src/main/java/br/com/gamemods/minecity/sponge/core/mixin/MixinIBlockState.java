package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockStateData;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IBlockState.class)
public interface MixinIBlockState extends SupplierBlockStateData
{
    @NotNull
    @Override
    default BlockStateData getBlockStateData()
    {
        return ReactiveLayer.getBlockManipulator().getBlockState(this).get();
    }
}
