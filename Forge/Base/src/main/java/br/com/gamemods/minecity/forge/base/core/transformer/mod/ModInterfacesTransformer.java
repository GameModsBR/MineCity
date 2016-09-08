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

        r.put("codechicken.microblock.ItemMicroPart",
                "br.com.gamemods.minecity.forge.base.protection.forgemultipart.IItemMicroPart");

        r.put("codechicken.multipart.minecraft.ButtonPart",
                "br.com.gamemods.minecity.forge.base.protection.forgemultipart.IButtonPart");

        r.put("codechicken.multipart.minecraft.LeverPart",
                "br.com.gamemods.minecity.forge.base.protection.forgemultipart.ILeverPart");

        r.put("codechicken.multipart.TileMultipart",
                "br.com.gamemods.minecity.forge.base.protection.forgemultipart.ITileMultiPart");

        r.put("codechicken.multipart.TMultiPart",
                "br.com.gamemods.minecity.forge.base.protection.forgemultipart.ITMultiPart");

        r.put("codechicken.multipart.JItemMultiPart",
                "br.com.gamemods.minecity.forge.base.protection.forgemultipart.IJItemMultiPart");

        r.put("codechicken.multipart.BlockMultipart",
                "br.com.gamemods.minecity.forge.base.protection.forgemultipart.IBlockMultipart");

        r.put("codechicken.translocator.BlockTranslocator",
                "br.com.gamemods.minecity.forge.base.protection.translocators.IBlockTranslocator");

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
