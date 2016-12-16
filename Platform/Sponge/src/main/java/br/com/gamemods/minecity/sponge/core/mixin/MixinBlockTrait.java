package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedBlockTrait;
import net.minecraft.block.properties.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("ClassHasNoToStringMethod")
@Mixin(PropertyHelper.class)
public abstract class MixinBlockTrait<T extends Comparable<T>> implements MixedBlockTrait<T>
{
    private BlockTraitData<T> blockTraitData;

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public BlockTraitData<T> getBlockTraitData()
    {
        if(blockTraitData != null)
            return blockTraitData;

        return blockTraitData = (BlockTraitData<T>) ReactiveLayer.getBlockManipulator().getBlockTraitData(this).get();
    }
}
