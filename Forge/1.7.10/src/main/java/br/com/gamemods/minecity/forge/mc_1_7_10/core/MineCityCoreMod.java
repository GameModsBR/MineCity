package br.com.gamemods.minecity.forge.mc_1_7_10.core;

import br.com.gamemods.minecity.forge.base.accessors.IChunk;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.deploader.DepLoader;
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

@MCVersion("1.7.10")
@TransformerExclusions("br.com.gamemods.minecity.forge.mc_1_7_10.core")
@Name("MineCityCore")
@SortingIndex(value = 1001)
public class MineCityCoreMod implements IFMLLoadingPlugin, IFMLCallHook
{
    @Override
    public Void call() throws Exception
    {
        File mcDir = (File) FMLInjectionData.data()[6];
        new DepLoader(new File(mcDir, "MineCity/libs"), (LaunchClassLoader) IChunk.class.getClassLoader()).load();

        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[]{
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.WorldServerTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.ChunkTransformer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.EntityPlayerMPTransformer"
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
