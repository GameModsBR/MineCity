package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.IEntityOwnable;

import java.util.UUID;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface EntityOwnable
{
    default void detectOwner()
    {}

    default UUID getOwnerId()
    {
        IEntityOwnable ownable = (IEntityOwnable) this;
        return ownable.getOwnerId();
    }

    default IEntity getOwner()
    {
        IEntityOwnable ownable = (IEntityOwnable) this;
        return (IEntity) ownable.getOwner();
    }
}
