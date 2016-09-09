package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.forge.base.accessors.item.IItemSeeds;

import java.lang.reflect.Field;

public class ImmersiveHooks
{
    private static Field itemSeeds;
    private static Field itemMaterial;
    private static Class<?> classIEContent;

    private static Class<?> getIEContent() throws ReflectiveOperationException
    {
        if(classIEContent == null)
            classIEContent = Class.forName("blusunrize.immersiveengineering.common.IEContent");
        return classIEContent;
    }

    public static IItemSeeds getItemSeeds()
    {
        try
        {
            if(itemSeeds == null)
                itemSeeds = getIEContent().getDeclaredField("itemSeeds");
            return (IItemSeeds) itemSeeds.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static IItemSeeds getItemMaterial()
    {
        try
        {
            if(itemMaterial == null)
                itemMaterial = getIEContent().getDeclaredField("itemMaterial");
            return (IItemSeeds) itemMaterial.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }
}
