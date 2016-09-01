package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityAgeable;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.passive.EntityAnimal;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityAnimal extends IEntityAgeable
{
    @Override
    default EntityAnimal getForgeEntity()
    {
        return (EntityAnimal) this;
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.ANIMAL;
    }
}
