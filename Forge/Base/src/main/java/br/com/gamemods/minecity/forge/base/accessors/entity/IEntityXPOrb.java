package br.com.gamemods.minecity.forge.base.accessors.entity;

import org.jetbrains.annotations.NotNull;

public interface IEntityXPOrb extends IEntity
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.ITEM;
    }
}
