package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityTransformer;

@Referenced(at = FrostEntityTransformer.class)
public interface FrostEntity extends IEntity, ICommander
{
    @Override
    default String getName()
    {
        return IEntity.super.getName();
    }
}
