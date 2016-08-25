package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

public class EntityTransformer extends InsertInterfaceTransformer
{
    public EntityTransformer()
    {
        super("net.minecraft.entity.entity", "br.com.gamemods.minecity.forge.base.accessors.IEntity");
    }

    public EntityTransformer(String interfaceClass)
    {
        super("net.minecraft.entity.entity", interfaceClass);
    }
}
