package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.Arrays;

@Referenced
public class ItemModifyFaceReactorTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ItemModifyFaceReactorTransformer()
    {
        super("br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyFaceReactor", Arrays.asList(
                "net.minecraft.item.ItemFlintAndSteel",
                "net.minecraft.item.ItemFireball",
                "net.minecraft.item.ItemArmorStand",
                "net.minecraft.item.ItemMonsterPlacer"
        ));
    }
}
