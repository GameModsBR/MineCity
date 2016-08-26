package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class ItemBlockTransformer extends InsertInterfaceTransformer
{
    public ItemBlockTransformer(String interfaceClass)
    {
        super("net.minecraft.item.ItemBlock", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ItemBlockTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.item.IItemBlock");
    }
}
