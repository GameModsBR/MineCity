package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class ItemStackTransformer extends InsertInterfaceTransformer
{
    public ItemStackTransformer(String interfaceClass)
    {
        super("net.minecraft.item.ItemStack", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ItemStackTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.item.IItemStack");
    }
}
