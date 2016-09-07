package br.com.gamemods.minecity.forge.base.core.transformer.mod;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.HashMap;
import java.util.Map;

@Referenced
public class ModInterfacesTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ModInterfacesTransformer()
    {
        Map<String, String> r = new HashMap<>();

        r.put("codechicken.enderstorage.storage.item.ItemEnderPouch",
                "br.com.gamemods.minecity.forge.base.protection.enderstorage.IItemEnderPouch");

        r.put("codechicken.enderstorage.common.ItemEnderStorage",
                "br.com.gamemods.minecity.forge.base.protection.enderstorage.IItemEnderStorage");

        r.put("codechicken.enderstorage.common.TileFrequencyOwner",
                "br.com.gamemods.minecity.forge.base.protection.enderstorage.ITileFrequencyOwner");

        r.put("codechicken.enderstorage.common.BlockEnderStorage",
                "br.com.gamemods.minecity.forge.base.protection.enderstorage.IBlockEnderStorage");

        setReplacements(r);
        printReplacements();
    }
}
