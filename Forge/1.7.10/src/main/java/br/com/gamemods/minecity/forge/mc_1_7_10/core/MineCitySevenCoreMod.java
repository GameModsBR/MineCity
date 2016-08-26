package br.com.gamemods.minecity.forge.mc_1_7_10.core;

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
        return new String[]{
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenWorldServerTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenChunkTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenEntityPlayerMPTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenEntityTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenBlockTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.ServerConfigurationManagerTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenMinecraftServerTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenBlockSnapshotTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenItemDyeTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockOpenReactorTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenItemStackTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemModifyOppositeReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockTNTTransformer"
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
        return null;
    }
}
