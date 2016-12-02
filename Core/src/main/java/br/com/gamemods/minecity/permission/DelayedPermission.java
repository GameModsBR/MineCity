package br.com.gamemods.minecity.permission;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.economy.DelayedProxy;
import org.jetbrains.annotations.NotNull;

public class DelayedPermission extends DelayedProxy<PermissionProxy> implements PermissionProxy
{
    public DelayedPermission(@NotNull MineCity mineCity, @NotNull PermissionProvider provider)
    {
        super(mineCity, provider);
    }

    @Override
    public boolean hasPermission(CommandSender sender, Permission perm)
    {
        return provide().hasPermission(sender, perm);
    }
}
