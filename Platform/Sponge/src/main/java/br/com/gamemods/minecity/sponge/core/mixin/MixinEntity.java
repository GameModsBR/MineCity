package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedEntity;
import br.com.gamemods.minecity.sponge.data.value.SpongeMinecraftEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("ClassHasNoToStringMethod")
@Mixin(Entity.class)
public abstract class MixinEntity implements MixedEntity
{
    @Nullable
    private SpongeMinecraftEntity mineCityEntity;
    @Nullable
    private EntityData entityData;

    @NotNull
    @Override
    public EntityData getEntityData()
    {
        if(entityData != null)
            return entityData;

        return entityData = ReactiveLayer.getEntityManipulator().getEntityData(this).get();
    }

    @Override
    public SpongeMinecraftEntity getMinecraftEntity()
    {
        return mineCityEntity;
    }

    @Override
    public void setMinecraftEntity(SpongeMinecraftEntity entity)
    {
        mineCityEntity = entity;
    }
}
