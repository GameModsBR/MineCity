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

        r.put("blusunrize.immersiveengineering.common.blocks.cloth.BlockClothDevices",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockModifyExtendsOpen");

        r.put("blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntityWindmill");

        r.put("blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileOpenOnClick");

        r.put("blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileOpenOnClick");

        r.put("blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileModifyOnHammer");

        r.put("blusunrize.immersiveengineering.common.blocks.wooden.BlockWoodenDevices",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockWoodenDevices");

        r.put("blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntityWoodenBarrel");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityChargingStation",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntityChargingStation");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileModifyOnHammer");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileModifyOnHammer");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileModifyOnHammer");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntityEnergyMeter");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityRedstoneBreaker",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntityRedstoneBreaker");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntityBreakerSwitch");

        r.put("blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces$IColouredTile",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IIColouredTile");

        r.put("blusunrize.immersiveengineering.api.tool.ChemthrowerHandler$ChemthrowerEffect",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IChemthrowerEffect");

        r.put("blusunrize.immersiveengineering.common.items.ItemIESeed",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IItemIESeed");

        r.put("blusunrize.immersiveengineering.common.items.ItemIEBase",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IItemIEBase");

        r.put("blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockIECrop");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockMetalDevices2");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockMetalDevices");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntitySampleDrill");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ITileEntityConveyorSorter");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityFurnaceHeater",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileModifyOnHammer");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileModifyOnHammer");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveTileModifyOnHammer");

        r.put("blusunrize.immersiveengineering.common.items.ItemWireCoil",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IItemWireCoil");

        r.put("blusunrize.immersiveengineering.api.energy.IImmersiveConnectable",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IConnectable");

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
