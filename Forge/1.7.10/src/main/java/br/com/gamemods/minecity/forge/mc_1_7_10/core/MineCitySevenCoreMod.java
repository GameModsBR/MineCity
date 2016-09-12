package br.com.gamemods.minecity.forge.mc_1_7_10.core;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.deploader.DepLoader;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.versioning.ComparableVersion;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.Map;

@Name("MineCityCore")
@MCVersion("1.7.10")
@TransformerExclusions({
    "br.com.gamemods.minecity.forge.mc_1_7_10.core",
    "br.com.gamemods.minecity.forge.base.core",
})
@SortingIndex(value = 1001)
public class MineCitySevenCoreMod implements IFMLLoadingPlugin, IFMLCallHook
{
    @Override
    public Void call() throws Exception
    {
        File mcDir = (File) FMLInjectionData.data()[6];
        new DepLoader(
                new File(mcDir, "MineCity/libs"),
                (LaunchClassLoader) MineCitySevenCoreMod.class.getClassLoader(),
                FMLInjectionData.data(),
                Loader.class,
                ComparableVersion::new
        ).load();

        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        ModEnv.hookClass = "br.com.gamemods.minecity.forge.mc_1_7_10.protection.MineCitySevenHooks";
        ModEnv.rayTraceResultClass = "net.minecraft.util.MovingObjectPosition";
        ModEnv.seven = true;

        return new String[]{
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.QuarryFixerBlockTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.BlockSulfurTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ItemIEToolTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ChemthrowerEffectTeleportTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.ChemthrowerHandlerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.EntityChemthrowerShotTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.ButtonPartTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.BlockMultiPartTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.EventHandlerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.PathFinderTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.world.ChunkCacheTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.OnImpactTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityEggTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockStemTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockSaplingTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.GrowMonitorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockDragonEggTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityLivingBaseTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityXPOrbTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityArrowTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityIgnitionTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityEnderCrystalTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockTNTTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityFishingHookTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityPotionTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityBoatTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityMinecartTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.world.SevenWorldServerTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.world.SevenChunkTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityPlayerMPTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockOpenReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockClickReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockClickExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockModifyExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockNoReactExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemModifyOppositeReactorTransformer",
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
        return "br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenAT";
    }
}
