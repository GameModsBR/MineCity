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

        r.put("thaumcraft.common.entities.golems.EntityGolemBase",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IEntityGolemBase");

        r.put("thaumcraft.common.blocks.BlockStoneDevice",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockStoneDevice");

        r.put("thaumcraft.common.blocks.BlockJar",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockJar");

        r.put("thaumcraft.common.blocks.BlockMetalDevice",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockMetalDevice");

        r.put("thaumcraft.common.items.equipment.ItemElementalShovel",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IItemElementalShovel");

        r.put("thaumcraft.common.items.ItemResource",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IItemResource");

        r.put("thaumcraft.common.items.wands.foci.ItemFocusHellbat",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IItemFocusHellbat");

        r.put("thaumcraft.common.entities.monster.EntityFireBat",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IEntityFireBat");

        r.put("thaumcraft.common.items.wands.foci.ItemFocusTrade",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IItemFocusTrade");

        r.put("thaumcraft.common.tiles.TileWarded",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ITileWarded");

        r.put("thaumcraft.common.items.wands.foci.ItemFocusWarding",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IItemFocusWarding");

        r.put("thaumcraft.common.blocks.BlockAiry",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockAiry");

        r.put("thaumcraft.common.entities.projectile.EntityShockOrb",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IEntityShockOrb");

        r.put("thaumcraft.common.tiles.TileNode",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ITileNode");

        r.put("thaumcraft.common.blocks.BlockArcaneDoor",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockArcaneDoor");

        r.put("thaumcraft.api.wands.ItemFocusBasic",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IItemFocusBasic");

        r.put("thaumcraft.api.wands.IWandTriggerManager",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.WandTriggerManager");

        r.put("thaumcraft.api.wands.IWandable",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.Wandable");

        r.put("thaumcraft.common.tiles.TileOwned",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ITileOwned");

        r.put("thaumcraft.common.items.wands.ItemWandCasting",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IItemWandCasting");

        r.put("thaumcraft.common.blocks.BlockWoodenDevice",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockWoodenDevice");

        r.put("thaumcraft.common.blocks.BlockCosmeticOpaque",
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockCosmeticOpaque");

        r.put("appeng.block.misc.BlockInterface",
                "br.com.gamemods.minecity.forge.base.protection.appeng.ICableBusPart");

        r.put("appeng.fmp.CableBusPart",
                "br.com.gamemods.minecity.forge.base.protection.appeng.ICableBusPart");

        r.put("appeng.items.parts.ItemFacade",
                "br.com.gamemods.minecity.forge.base.protection.appeng.IItemMultiPart");

        r.put("appeng.items.parts.ItemMultiPart",
                "br.com.gamemods.minecity.forge.base.protection.appeng.IItemMultiPart");

        r.put("appeng.items.tools.quartz.ToolQuartzWrench",
                "br.com.gamemods.minecity.forge.base.protection.appeng.IItemToolQuartzWrench");

        r.put("appeng.items.tools.powered.ToolEntropyManipulator",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyReactor");

        r.put("appeng.items.tools.powered.ToolColorApplicator",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyReactor");

        r.put("appeng.items.tools.ToolNetworkTool",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyReactor");

        r.put("appeng.block.AEBaseItem",
                "br.com.gamemods.minecity.forge.base.protection.appeng.IAEBaseItem");

        r.put("appeng.block.AEBaseItemBlock",
                "br.com.gamemods.minecity.forge.base.protection.appeng.IAEBaseItemBlock");

        r.put("appeng.block.misc.BlockTinyTNT",
                "br.com.gamemods.minecity.forge.base.protection.appeng.IBlockTinyTNT");

        r.put("appeng.block.AEBaseTileBlock",
                "br.com.gamemods.minecity.forge.base.protection.appeng.IAEBaseTileBlock");

        r.put("ic2.core.item.ItemRadioactive",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.RadioactiveItem");

        r.put("ic2.core.item.reactor.ItemReactorUranium",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.RadioactiveItem");

        r.put("ic2.core.item.ItemScrapbox",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemScrapbox");

        r.put("ic2.core.block.machine.tileentity.TileEntityTeleporter",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ITileEntityTeleporter");

        r.put("ic2.core.item.tool.ItemFrequencyTransmitter",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemFrequencyTransmitter");

        r.put("ic2.core.item.ItemIC2",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemIC2");

        r.put("ic2.core.item.tool.ItemWeedingTrowel",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemWeedingTrowel");

        r.put("ic2.core.crop.cropcard.CropWeed",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICropWeeds");

        r.put("ic2.core.crop.CropWeed",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICropWeeds");

        r.put("ic2.core.crop.cropcard.CropVenomilia",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICropVenomillia");

        r.put("ic2.core.crop.CropVenomilia",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICropVenomillia");

        r.put("ic2.core.crop.BlockCrop",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IBlockCropIC2");

        r.put("ic2.core.block.BlockScaffold",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IBlockScaffold");

        r.put("ic2.core.item.tool.ItemSprayer",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemSprayer");

        r.put("ic2.core.item.ItemResin",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemResin");

        r.put("ic2.core.item.tool.ItemTreetapElectric",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.TreeTap");

        r.put("ic2.core.item.tool.ItemTreetap",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.TreeTap");

        r.put("ic2.core.item.ItemMulti",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemMulti");

        r.put("ic2.core.item.resources.ItemCell",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemFluidCell");

        r.put("ic2.core.item.ItemFluidCell",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IItemFluidCell");

        r.put("ic2.core.block.BlockFoam",
                "br.com.gamemods.minecity.forge.base.protection.industrialcraft.IBlockFoam");

        r.put("ic2.core.item.tool.ItemToolPainter",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyFirstReactor");

        r.put("ic2.core.item.tool.ItemToolCutter",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyReactor");

        r.put("ic2.core.item.tool.ItemObscurator",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyFirstReactor");

        r.put("ic2.core.item.crafting.UpgradeKit",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyFirstReactor");

        r.put("ic2.core.item.ItemUpgradeKit",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyFirstReactor");

        r.put("ic2.core.item.tool.ItemToolWrench",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyFirstReactor");

        r.put("mcheli.wrapper.W_EntityContainer",
                "br.com.gamemods.minecity.forge.base.protection.mcheli.W_EntityContainer");

        r.put("mcheli.wrapper.W_Entity",
                "br.com.gamemods.minecity.forge.base.protection.mcheli.IWEntity");

        r.put("mcheli.aircraft.MCH_EntityAircraft",
                "br.com.gamemods.minecity.forge.base.protection.mcheli.IEntityAircraft");

        r.put("li.cil.oc.common.tileentity.Adapter",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IAdapter");

        r.put("li.cil.oc.server.component.traits.InventoryAware",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IInventoryAware");

        r.put("li.cil.oc.server.component.traits.InventoryWorldControlMk2",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IInventoryWorldControlMk2");

        r.put("li.cil.oc.common.item.Delegator",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IDelegator");

        r.put("li.cil.oc.common.tileentity.Robot",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IRobotTile");

        r.put("li.cil.oc.server.component.Agent",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IAgentComponent");

        r.put("li.cil.oc.api.machine.MachineHost",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IMachineHost");

        r.put("li.cil.oc.api.internal.Agent",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IAgent");

        r.put("li.cil.oc.common.entity.Drone",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IDrone");

        r.put("li.cil.oc.common.block.RobotAfterimage",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IRobotAfterimage");

        r.put("li.cil.oc.common.block.RobotProxy",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IRobotProxy");

        r.put("li.cil.oc.common.block.Microcontroller",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IMicrocontroller");

        r.put("li.cil.oc.common.block.NetSplitter",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.INetSplitter");

        r.put("li.cil.oc.common.block.Transposer",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.Redstone",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.PowerDistributor",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.PowerConverter",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.MotionSensor",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.Hologram",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.Geolyzer",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.Charger",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockOpenUpdate");

        r.put("li.cil.oc.common.block.FakeEndstone",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.ChameliumBlock",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.common.block.Cable",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlockNoReaction");

        r.put("li.cil.oc.api.network.EnvironmentHost",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IEnvironmentHost");

        r.put("li.cil.oc.common.block.Keyboard",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IKeyboard");

        r.put("li.cil.oc.api.internal.Rotatable",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IRotatable");

        r.put("li.cil.oc.common.block.Screen",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IScreen");

        r.put("li.cil.oc.common.block.SimpleBlock",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.ISimpleBlock");

        r.put("li.cil.oc.common.tileentity.traits.Colored",
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.IColored");

        r.put("com.pam.harvestcraft.BlockPamCrop",
                "br.com.gamemods.minecity.forge.base.protection.pamharvestcraft.IBlockPamCrop");

        r.put("com.pam.harvestcraft.ItemPamSeedFood",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemSeedFood");

        r.put("com.pam.harvestcraft.BlockPamFruit",
                "br.com.gamemods.minecity.forge.base.protection.pamharvestcraft.IBlockFruit");

        r.put("com.pam.harvestcraft.BlockPamWaterGarden",
                "br.com.gamemods.minecity.forge.base.protection.pamharvestcraft.IBlockGardern");

        r.put("com.pam.harvestcraft.BlockPamNormalGarden",
                "br.com.gamemods.minecity.forge.base.protection.pamharvestcraft.IBlockGardern");

        r.put("com.pam.harvestcraft.BlockPamMushroomGarden",
                "br.com.gamemods.minecity.forge.base.protection.pamharvestcraft.IBlockGardern");

        r.put("com.pam.harvestcraft.BlockPamDesertGarden",
                "br.com.gamemods.minecity.forge.base.protection.pamharvestcraft.IBlockGardern");

        r.put("com.pam.harvestcraft.BlockPamSink",
                "br.com.gamemods.minecity.forge.base.protection.pamharvestcraft.IBlockPamSink");

        r.put("com.pam.harvestcraft.BlockPamQuern",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamPresser",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamPot",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamOven",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamMarket",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamFishTrap",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamCuttingBoard",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamChurn",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamApiary",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.pam.harvestcraft.BlockPamAnimalTrap",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockWindowDecorationClosed",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockWindowDecoration",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockWallCabinet",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockToaster",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenForceSyncReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockTap",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IBlockTap");

        r.put("com.mrcrayfish.furniture.blocks.BlockTV",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.mrcrayfish.furniture.blocks.BlockStereo",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.mrcrayfish.furniture.blocks.BlockShowerHead",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockShower",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IBlockBath");

        r.put("com.mrcrayfish.furniture.blocks.BlockPlate",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenForceSyncReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockMicrowave",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockLampOff",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.mrcrayfish.furniture.blocks.BlockLampOn",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.mrcrayfish.furniture.blocks.BlockFridge",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockCookieJar",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenForceSyncReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockComputer",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.mrcrayfish.furniture.blocks.BlockChoppingBoard",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenForceSyncReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockCabinetKitchen",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockCabinet",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockBlender",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenForceSyncReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockBin",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen");

        r.put("com.mrcrayfish.furniture.blocks.BlockBin",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen");

        r.put("com.mrcrayfish.furniture.blocks.BlockBedsideCabinet",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenGUIReactor");

        r.put("com.mrcrayfish.furniture.blocks.BlockBath",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IBlockBath");

        r.put("com.mrcrayfish.furniture.blocks.BlockBasin",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IBlockBasin");

        r.put("com.mrcrayfish.furniture.blocks.BlockCouch",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IBlockSitRecolor");

        r.put("com.mrcrayfish.furniture.blocks.BlockBarStool",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IBlockSitRecolor");

        r.put("com.mrcrayfish.furniture.blocks.BlockSittable",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IBlockSittable");

        r.put("com.mrcrayfish.furniture.entity.EntitySittableBlock",
                "br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture.IEntitySittableBlock");

        r.put("com.goldensilver853.vehicles.items.VehicularItem",
                "br.com.gamemods.minecity.forge.base.protection.vehicularmovement.IVehicularItem");

        r.put("com.goldensilver853.vehicles.entity.VehicularEntity",
                "br.com.gamemods.minecity.forge.base.protection.vehicularmovement.IVehicularEntity");

        r.put("codechicken.wirelessredstone.logic.ReceiverPart",
                "br.com.gamemods.minecity.forge.base.protection.wrcbe.DevicePart");

        r.put("codechicken.wirelessredstone.logic.WirelessPart",
                "br.com.gamemods.minecity.forge.base.protection.wrcbe.DevicePart");

        r.put("br.com.gamemods.universalcoinsserver.blocks.BlockTradeStation",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen");

        r.put("br.com.gamemods.universalcoinsserver.blocks.BlockSlots",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen");

        r.put("br.com.gamemods.universalcoinsserver.blocks.BlockPackager",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen");

        r.put("br.com.gamemods.universalcoinsserver.blocks.BlockCardStation",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockNoReactionExtendsOpen");

        r.put("br.com.gamemods.universalcoinsserver.blocks.BlockOwned",
                "br.com.gamemods.minecity.forge.base.protection.universalcoinsserver.IBlockOwned");

        r.put("br.com.gamemods.universalcoinsserver.blocks.PlayerOwned",
                "br.com.gamemods.minecity.forge.base.protection.universalcoinsserver.IPlayerOwned");

        r.put("br.com.gamemods.universalcoinsserver.blocks.BlockAdvSign",
                "br.com.gamemods.minecity.forge.base.protection.universalcoinsserver.IBlockAdvSign");

        r.put("cpw.mods.ironchest.ItemChestChanger",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemModifyFirstReactor");

        r.put("unwrittenfun.minecraft.immersiveintegration.blocks.BlockItemRobin",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockModifyExtendsOpen");

        r.put("unwrittenfun.minecraft.immersiveintegration.blocks.BlockExtendablePost",
                "br.com.gamemods.minecity.forge.base.protection.immersiveintegrations.IBlockExtendablePost");

        r.put("com.bymarcin.zettaindustries.mods.ecatalogue.ECatalogueBlock",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.bymarcin.zettaindustries.mods.nfc.block.BlockNFCReader",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.bymarcin.zettaindustries.mods.nfc.block.BlockNFCProgrammer",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.bymarcin.zettaindustries.mods.nfc.smartcard.SmartCardTerminalBlock",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen");

        r.put("com.bymarcin.zettaindustries.mods.battery.block.BlockBigBatteryPowerTap",
                "br.com.gamemods.minecity.forge.base.protection.zettaindustries.IBlockBigBatteryPowerTap");

        r.put("com.bymarcin.zettaindustries.mods.battery.block.BlockBigBatteryController",
                "br.com.gamemods.minecity.forge.base.protection.zettaindustries.IBigBatteryController");

        r.put("com.bymarcin.zettaindustries.mods.battery.block.BasicBlockMultiblockBase",
                "br.com.gamemods.minecity.forge.base.protection.zettaindustries.IBigBattery");

        r.put("com.bymarcin.zettaindustries.mods.vanillautils.block.VariableRedstoneEmitter",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockModifyExtendsOpen");

        r.put("com.bymarcin.zettaindustries.mods.rfpowermeter.RFMeterBlock",
                "br.com.gamemods.minecity.forge.base.protection.zettaindustries.IRFMeterBlock");

        r.put("com.bymarcin.zettaindustries.mods.wiregun.EntityHookBullet",
                "br.com.gamemods.minecity.forge.base.protection.zettaindustries.IEntityHookBullet");

        r.put("blusunrize.immersiveengineering.common.entities.EntityRevolvershot",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IEntityRevolverShot");

        r.put("blusunrize.immersiveengineering.common.items.ItemIETool",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IItemIETool");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration2",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockDecoration");

        r.put("blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockDecoration");

        r.put("blusunrize.immersiveengineering.common.blocks.wooden.BlockWoodenDecoration",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockDecoration");

        r.put("blusunrize.immersiveengineering.common.blocks.stone.BlockStoneDecoration",
                "br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IBlockDecoration");

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

        r.put("blusunrize.immersiveengineering.api.energy.IWireCoil",
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
