package br.com.gamemods.minecity.permission;

import br.com.gamemods.minecity.api.command.CommandSender;

public interface PermissionProxy
{
    boolean hasPermission(CommandSender sender, Permission perm);
}
