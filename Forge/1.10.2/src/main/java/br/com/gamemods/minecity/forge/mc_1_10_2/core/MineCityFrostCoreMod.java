package br.com.gamemods.minecity.forge.mc_1_10_2.core;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.deploader.DepLoader;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import java.io.File;
import java.util.Map;

@Name("MineCityCore")
@MCVersion("1.10.2")
@TransformerExclusions({
    "br.com.gamemods.minecity.forge.mc_1_10_2.core",
    "br.com.gamemods.minecity.forge.base.core",
})
@SortingIndex(value = 1001)
public class MineCityFrostCoreMod implements IFMLLoadingPlugin, IFMLCallHook
{
    @Override
    public Void call() throws Exception
    {
        File mcDir = (File) FMLInjectionData.data()[6];
        new DepLoader(
                new File(mcDir, "MineCity/libs"),
                (LaunchClassLoader) MineCityFrostCoreMod.class.getClassLoader(),
                FMLInjectionData.data(),
                Loader.class,
                ComparableVersion::new
        ).load();

        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        ModEnv.hookClass = "br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks";
        ModEnv.rayTraceResultClass = "net.minecraft.util.math.RayTraceResult";

        return new String[]{
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityIC2ExplosiveTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityDynamiteTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.ICropTileTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.CropCardTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.TileEntityCropTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityParticleTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.InventoryTransferDClassTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TransposerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.AdapterTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.InventoryWorldControlMk2DClassTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.MagnetProviderTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.UpgradePistonTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.UpgradeTractorBeamTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.UpgradeLeashTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TankWorldControlDClassTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TileRobotProxyTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.InventoryWorldControlDClassTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TextBufferTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.PacketHandlerDTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.pamharvestcraft.BlockPamSaplingTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.mrcrayfishfurniture.MessageTVServerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbe.EntityREPTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbe.JammerPartTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbe.WirelessBoltTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveintegration.TileItemRobinTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.QuarryFixerBlockTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.BlockSulfurTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.TileEntityFluidPumpTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.TileEntityConveyorSorterTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.BlockMetalDevicesTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ItemIEToolTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ChemthrowerEffectTeleportTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ChemthrowerHandlerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.EntityChemthrowerShotTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.ButtonPartTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.BlockMultiPartTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.EventHandlerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockPistonBaseTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockPistonBaseTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemBucketTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockChorusFlowerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.NodeProcessorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.PathFinderTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.world.ChunkCacheTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.OnImpactTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityEggTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockStemTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockSaplingTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.GrowMonitorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockDragonEggTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityXPOrbTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityArrowTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityIgnitionTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityEnderCrystalTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockTNTTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityArmorStandTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityFishingHookTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityAreaEffectCloudTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityPotionTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityBoatTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityMinecartTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostWorldServerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.world.ChunkTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityPlayerMPTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockOpenReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockClickReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockClickExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockModifyExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockNoReactExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemModifyFaceReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.ProjectileTransformer"
        };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return getClass().getName();
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        // Nothing needs to be injected here
    }

    @Override
    public String getAccessTransformerClass()
    {
        return "br.com.gamemods.minecity.forge.base.core.transformer.MineCityAT";
    }
}
