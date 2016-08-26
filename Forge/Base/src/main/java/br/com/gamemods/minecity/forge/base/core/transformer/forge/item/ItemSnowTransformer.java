package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class ItemSnowTransformer extends InsertInterfaceTransformer
{
    public ItemSnowTransformer(String interfaceClass)
    {
        super("net.minecraft.item.ItemSnow", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ItemSnowTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.item.IItemSnow");
    }
}
