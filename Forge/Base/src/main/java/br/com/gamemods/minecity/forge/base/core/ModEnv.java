package br.com.gamemods.minecity.forge.base.core;

import br.com.gamemods.minecity.forge.base.protection.vanilla.BlockProtections;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;

import java.io.File;
import java.io.FileOutputStream;

public class ModEnv
{
    public static final String MOD_ID = "minecity";
    public static final String MOD_NAME = "MineCity";
    public static final String MOD_VERSION = "1.0.0-SNAPSHOT";

    public static String hookClass;
    public static String rayTraceResultClass;
    public static EntityProtections entityProtections;
    public static BlockProtections blockProtections;
    public static boolean seven;

    public static boolean saveClasses = true;

    public static byte[] saveClass(String srg, byte[] bytes)
    {
        if(!saveClasses)
            return bytes;

        File file = new File("MineCityPatch");
        if(!file.isDirectory() && !file.mkdirs())
            System.err.println("Failed to create dir: "+file);

        try(FileOutputStream out = new FileOutputStream(new File(file, srg+".class")))
        {
            out.write(bytes);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return bytes;
    }
}
