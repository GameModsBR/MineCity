package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class ItemDyeTransformer extends InsertInterfaceTransformer
{
    public ItemDyeTransformer(String interfaceClass)
    {
        super("net.minecraft.item.ItemDye", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ItemDyeTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.IItemDye");
    }
}
