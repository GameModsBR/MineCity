package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.PacketHandlerDTransformer;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;

public class OCHooks
{
    private static Object colorModule;
    private static Method isDye;

    private static Object getColorModule()
    {
        try
        {
            if(colorModule == null)
                colorModule = Class.forName("li.cil.oc.util.Color$").getDeclaredField("MODULE$").get(null);

            return colorModule;
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean isDye(ItemStack stack)
    {
        try
        {
            Object module = getColorModule();
            if(isDye == null)
                isDye = Class.forName("li.cil.oc.util.Color$").getDeclaredMethod("isDye", ItemStack.class);
            return (boolean) isDye.invoke(module, stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = PacketHandlerDTransformer.class)
    public static boolean onPlayerInteract(ITextBuffer buffer, IEntityPlayerMP player)
    {
        MineCityForge mod = player.getServer();

        IEnvironmentHost host = buffer.hostI();
        BlockPos pos = host.envBlockPos(mod);
        return mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(player, PermissionFlag.CLICK).isPresent();
    }
}
