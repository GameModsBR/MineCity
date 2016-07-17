package br.com.gamemods.minecity.forge.core;

import br.com.gamemods.minecity.forge.core.deploader.DepLoader;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.Map;

@MCVersion("1.7.10")
@TransformerExclusions("br.com.gamemods.minecity.forge.core")
@Name("MineCityCore")
@SortingIndex(value = 1001)
public class MineCityCoreMod implements IFMLLoadingPlugin, IFMLCallHook
{
    @Override
    public Void call() throws Exception
    {
        File mcDir = (File) FMLInjectionData.data()[6];
        new DepLoader(new File(mcDir, "MineCity/libs"), (LaunchClassLoader) DepLoader.class.getClassLoader()).load();

        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[0];
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

    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
