package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.permission.Permission;
import br.com.gamemods.minecity.permission.PermissionProvider;
import br.com.gamemods.minecity.permission.PermissionProxy;
import net.minecraft.entity.Entity;

import java.lang.reflect.Method;

public class CauldronPermission implements PermissionProxy
{
    public static final PermissionProvider PROVIDER = mineCity ->
    {
        try
        {
            return new CauldronPermission();
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException("Bukkit's SuperPerms is not available on this server", e);
        }
    };

    private Method getBukkitEntity;
    private Method hasPermission;
    private Class<?> Permissible;

    public CauldronPermission() throws ReflectiveOperationException
    {
        getBukkitEntity = Entity.class.getDeclaredMethod("getBukkitEntity");
        Permissible = Class.forName("org.bukkit.permissions.Permissible");
        hasPermission = Permissible.getDeclaredMethod("hasPermission", String.class);
    }

    @Override
    public boolean hasPermission(CommandSender sender, Permission perm)
    {
        Object handler = sender.getHandler();
        try
        {
            if(handler instanceof Entity)
            {
                Object bukkitEntity = getBukkitEntity.invoke(handler);
                if(Permissible.isInstance(bukkitEntity))
                    return (boolean) hasPermission.invoke(bukkitEntity, perm.getKey());
            }
        }
        catch(ReflectiveOperationException e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
