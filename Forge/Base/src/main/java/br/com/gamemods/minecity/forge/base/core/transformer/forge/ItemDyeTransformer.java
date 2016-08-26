package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

public class ItemDyeTransformer extends InsertInterfaceTransformer
{
    public ItemDyeTransformer(String interfaceClass)
    {
        super("net.minecraft.item.ItemDye", interfaceClass);
    }

    public ItemDyeTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.IItemDye");
    }
}
