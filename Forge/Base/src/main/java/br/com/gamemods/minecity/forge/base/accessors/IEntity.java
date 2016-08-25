package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.EntityTransformer;
import net.minecraft.entity.Entity;

import java.util.List;

@Referenced(at = EntityTransformer.class)
public interface IEntity
{
    default Entity getForgeEntity()
    {
        return (Entity) this;
    }

    default Entity getVehicle()
    {
        return ((Entity) this).getRidingEntity();
    }

    default List<Entity> getPassengers()
    {
        Entity entity = (Entity) this;
        return entity.getPassengers();
    }

    @SuppressWarnings("unchecked")
    default List<IEntity> getIPassengers()
    {
        return (List) getPassengers();
    }

    default String getName()
    {
        Entity entity = (Entity) this;
        return entity.getName();
    }
}
