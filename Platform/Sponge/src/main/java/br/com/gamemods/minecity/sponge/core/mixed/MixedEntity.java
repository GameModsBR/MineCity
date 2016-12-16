package br.com.gamemods.minecity.sponge.core.mixed;

import br.com.gamemods.minecity.reactive.game.entity.data.supplier.SupplierEntityData;
import br.com.gamemods.minecity.reactive.game.entity.supplier.SupplierReactiveEntity;
import br.com.gamemods.minecity.sponge.data.value.SpongeMinecraftEntity;

public interface MixedEntity extends SupplierEntityData, SupplierReactiveEntity
{
    SpongeMinecraftEntity getMinecraftEntity();
    void setMinecraftEntity(SpongeMinecraftEntity entity);
}
