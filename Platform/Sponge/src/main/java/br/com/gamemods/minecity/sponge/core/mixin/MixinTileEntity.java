package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveTileEntity;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedTileEntity;
import br.com.gamemods.minecity.sponge.core.mixed.Reactive;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(TileEntity.class)
public abstract class MixinTileEntity implements MixedTileEntity
{
    @Nullable
    private TileEntityData tileEntityData;
    private Reactive<ReactiveTileEntity> reactiveTileEntity = new Reactive<>(()->
            ReactiveLayer.getBlockReactor().getTileEntity(getTileEntityData())
    );

    @NotNull
    @Override
    public TileEntityData getTileEntityData()
    {
        if(tileEntityData != null)
            return tileEntityData;

        return tileEntityData = ReactiveLayer.getBlockManipulator().getTileEntityData(this).get();
    }

    @NotNull
    @Override
    public Optional<ReactiveTileEntity> getReactiveTileEntity()
    {
        return reactiveTileEntity.get();
    }
}
