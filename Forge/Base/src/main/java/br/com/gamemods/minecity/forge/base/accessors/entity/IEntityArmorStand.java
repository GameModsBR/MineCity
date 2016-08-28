package br.com.gamemods.minecity.forge.base.accessors.entity;

import org.jetbrains.annotations.NotNull;

public interface IEntityArmorStand extends IEntityLivingBase
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.STORAGE;
    }
}
