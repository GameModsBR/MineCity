package br.com.gamemods.minecity.forge.mc_1_10_2.core;

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
        return new String[]{
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.IPropertyTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostWorldServerTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ChunkTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityPlayerMPTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostBlockTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostIBlockStateTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostPlayerListTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.MinecraftServerTransformer",
                "br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostBlockSnapshotTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemBlockTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemSnowTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemDyeTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockOpenReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockClickReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockClickExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockModifyExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockNoReactExtendsOpenTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemStackTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemModifyOppositeReactorTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockTNTTransformer",
                "br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockDoorTransformer"
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
