package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.Arrays;

@Referenced
public class ItemModifyOppositeReactorTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ItemModifyOppositeReactorTransformer()
    {
        super("br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyOppositeReactor", Arrays.asList(
                "net.minecraft.item.ItemFlintAndSteel",
                "net.minecraft.item.ItemFireball",
                "net.minecraft.item.ItemArmorStand"
        ));
    }
}
