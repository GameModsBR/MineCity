package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

public class EntityTransformer extends InsertInterfaceTransformer
{
    public EntityTransformer()
    {
        super("net.minecraft.entity.Entity", "br.com.gamemods.minecity.forge.base.accessors.entity.IEntity");
    }

    public EntityTransformer(String interfaceClass)
    {
        super("net.minecraft.entity.Entity", interfaceClass);
    }
}
