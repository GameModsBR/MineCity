package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import org.jetbrains.annotations.NotNull;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostEntity extends IEntity, ICommander
{
    @NotNull
    @Override
    default String getName()
    {
        return IEntity.super.getName();
    }
}
