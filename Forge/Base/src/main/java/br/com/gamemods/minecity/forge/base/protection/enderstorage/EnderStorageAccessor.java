package br.com.gamemods.minecity.forge.base.protection.enderstorage;

import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EnderStorageAccessor
{
    private static Method getDyeTypeMethod;
    private static Method getPersonalItemMethod;
    private static Field TileFrequencyOwner$owner;

    public static String getTileFrequencyOwner$owner(ITileEntity tile)
    {
        try
        {
            if(TileFrequencyOwner$owner == null)
            {
                TileFrequencyOwner$owner = Class.forName("codechicken.enderstorage.common.TileFrequencyOwner")
                        .getDeclaredField("owner");
                TileFrequencyOwner$owner.setAccessible(true);
            }

            return (String) TileFrequencyOwner$owner.get(tile);
        }
        catch(ReflectiveOperationException | NullPointerException e)
        {
            e.printStackTrace();
            return "$INCONSISTENCY$";
        }
    }

    public static ItemStack getPersonalItem()
    {
        try
        {
            if(getPersonalItemMethod == null)
            {
                getPersonalItemMethod = Class.forName("codechicken.enderstorage.EnderStorage")
                        .getDeclaredMethod("getPersonalItem");
            }

            return (ItemStack) getPersonalItemMethod.invoke(null);
        }
        catch(ReflectiveOperationException | NullPointerException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static int getDyeType(ItemStack stack)
    {
        try
        {
            if(getDyeTypeMethod == null)
            {
                getDyeTypeMethod = Class.forName("codechicken.enderstorage.common.EnderStorageRecipe")
                        .getDeclaredMethod("getDyeType", ItemStack.class);
            }

            return (Integer) getDyeTypeMethod.invoke(null, stack);

        }
        catch(ReflectiveOperationException | NullPointerException e)
        {
            e.printStackTrace();
            return 1;
        }
    }
}
