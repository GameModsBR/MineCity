package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.entity.Player;

public class BukkitPlayer implements CommandSender
{
    public final MineCityBukkit plugin;
    public final Player player;
    public final PlayerID playerId;

    public BukkitPlayer(MineCityBukkit plugin, Player player)
    {
        this.plugin = plugin;
        this.player = player;
        this.playerId = new PlayerID(player.getUniqueId(), player.getName());
    }

    @Override
    public BlockPos getPosition()
    {
        return plugin.blockPos(player.getLocation());
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return playerId;
    }

    @Override
    public void send(Message message)
    {
        player.sendMessage(message.toString());
    }
}
