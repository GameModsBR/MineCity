package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ModInterfacesTransformer.class)
public interface IDrone extends IEntity
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.UNCLASSIFIED;
    }
}
