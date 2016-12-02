package br.com.gamemods.minecity.permission;

import br.com.gamemods.minecity.api.command.CommandSender;

public class InternalPermission implements PermissionProxy
{
    @Override
    public boolean hasPermission(CommandSender sender, Permission permission)
    {
        String perm = permission.getKey();
        return sender.isOp() || !perm.contains("reload") && !perm.contains("admin")
                && !perm.contains("nature.deny") && !perm.contains("nature.allow") && !perm.contains("bypass")
                && !perm.contains("nature.rename");
    }
}
