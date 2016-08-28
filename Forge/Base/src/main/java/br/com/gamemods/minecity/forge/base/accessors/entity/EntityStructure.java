package br.com.gamemods.minecity.forge.base.accessors.entity;

import org.jetbrains.annotations.NotNull;

public interface EntityStructure extends IEntity
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.STRUCTURE;
    }
}
