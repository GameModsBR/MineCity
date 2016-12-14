package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTraitData;
import net.minecraft.block.properties.IProperty;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IProperty.class)
public interface MixinIProperty<T extends Comparable<T>> extends SupplierBlockTraitData<T>
{
    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    default BlockTraitData<T> getBlockTraitData()
    {
        return (BlockTraitData<T>) ReactiveLayer.getBlockManipulator().getBlockTraitData(this).get();
    }
}
