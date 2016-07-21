package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCityConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Locale;
import java.util.Optional;

public class MineCityPlugin extends JavaPlugin
{
    private MineCityBukkit mineCity;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        reloadConfig();

        @SuppressWarnings("SpellCheckingInspection")
        FileConfiguration yaml = getConfig();
        MineCityConfig config = new MineCityConfig();
        config.dbUrl = yaml.getString("database.url", config.dbUrl);
        config.dbUser = Optional.ofNullable(yaml.getString("database.user")).filter(u-> !u.isEmpty()).orElse(null);
        config.dbPass = Optional.ofNullable(yaml.getString("database.pass")).filter(p-> !p.isEmpty()).map(String::getBytes).orElse(null);
        config.locale = Locale.forLanguageTag(Optional.ofNullable(yaml.getString("language")).filter(l->!l.isEmpty()).orElse("en"));
        mineCity = new MineCityBukkit(this, config);
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
