package br.com.gamemods.minecity.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class MineCityPlugin extends JavaPlugin
{
    private MineCityBukkit mineCity;

    @Override
    public void onEnable()
    {
        mineCity = new MineCityBukkit(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return mineCity.onCommand(sender, label, args);
    }

    public BukkitScheduler getScheduler()
    {
        return getServer().getScheduler();
    }

    public PluginManager getPluginManager()
    {
        return getServer().getPluginManager();
    }
}
