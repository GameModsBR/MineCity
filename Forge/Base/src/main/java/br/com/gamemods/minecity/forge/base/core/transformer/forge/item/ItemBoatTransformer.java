package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class ItemBoatTransformer extends InsertInterfaceTransformer
{
    public ItemBoatTransformer(String interfaceClass)
    {
        super("net.minecraft.item.ItemBoat", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ItemBoatTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.item.IItemBoat");
    }
}
