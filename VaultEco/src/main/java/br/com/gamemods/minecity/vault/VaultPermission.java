package br.com.gamemods.minecity.vault;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.permission.PermissionProxy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VaultPermission implements PermissionProxy
{
    @NotNull
    private Permission vault;

    public VaultPermission(@NotNull Permission vault)
    {
        this.vault = vault;
    }

    @Override
    public boolean hasPermission(CommandSender sender, String perm)
    {
        Object handler = sender.getHandler();
        if(handler instanceof Player)
            return vault.has((Player) handler, perm);
        else if(handler instanceof CommandSender)
            return vault.has((org.bukkit.command.CommandSender) handler, perm);

        return sender.isPlayer() && vault.has((World) null, sender.getPlayerId().getName(), perm);
    }
}
