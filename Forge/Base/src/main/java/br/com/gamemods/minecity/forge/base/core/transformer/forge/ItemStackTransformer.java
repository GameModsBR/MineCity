package br.com.gamemods.minecity.forge.base.core.transformer.forge;

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
    public ItemStackTransformer(String className, String interfaceClass)
    {
        this("br.com.gamemods.minecity.forge.base.accessors.IItemStack");
    }
}
