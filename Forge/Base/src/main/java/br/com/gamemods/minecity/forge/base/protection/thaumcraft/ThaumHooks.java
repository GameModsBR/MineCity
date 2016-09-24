package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft.TileNodeTransformer;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Referenced
public class ThaumHooks
{
    private static Field triggers;
    private static Field configWardedStone;
    private static Method wandGetFocus;
    private static Method isOnCooldown;

    public static boolean isOnWandCooldown(IEntityPlayerMP player)
    {
        try
        {
            if(isOnCooldown == null)
            {
                isOnCooldown = Class.forName("thaumcraft.common.items.wands.WandManager").getDeclaredMethod("isOnCooldown", EntityLivingBase.class);
                isOnCooldown.setAccessible(true);
            }

            return (boolean) isOnCooldown.invoke(null, player);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static IItemFocusBasic getFocus(IItemWandCasting wand, IItemStack stack)
    {
        try
        {
            if(wandGetFocus == null)
                wandGetFocus = Class.forName("thaumcraft.common.items.wands.ItemWandCasting").getDeclaredMethod("getFocus", ItemStack.class);

            return (IItemFocusBasic) wandGetFocus.invoke(wand, stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean getConfigWardedStone()
    {
        try
        {
            if(configWardedStone == null)
                configWardedStone = Class.forName("thaumcraft.common.config.Config").getDeclaredField("wardedStone");
            return configWardedStone.getBoolean(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, HashMap<List, List>> getTriggers()
    {
        try
        {
            if(triggers == null)
            {
                triggers = Class.forName("thaumcraft.api.wands.WandTriggerRegistry").getDeclaredField("triggers");
                triggers.setAccessible(true);
            }

            return (Map) triggers.get(null);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = TileNodeTransformer.class)
    public static boolean onNodeBreak(TileEntity mcTile, World mcWorld, int x, int y, int z)
    {
        ITileEntity tile = (ITileEntity) mcTile;
        return ModHooks.onBlockAccessOther(
                mcWorld, x, y, z,
                tile.getPosX(), tile.getPosY(), tile.getPosZ(),
                PermissionFlag.MODIFY
        ).isPresent();
    }
}
