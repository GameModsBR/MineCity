package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.passive.EntityPig;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityPig extends IEntityAnimal
{
    @Override
    default EntityPig getForgeEntity()
    {
        return (EntityPig) this;
    }

    default boolean isSaddled()
    {
        return getForgeEntity().getSaddled();
    }

    default void setSaddled(boolean val)
    {
        getForgeEntity().setSaddled(val);
    }
}
