package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;

public class MineCityPlugin extends JavaPlugin
{
    private MineCityBukkit instance;
    private BukkitTask reloadTask;

    @Override
    public void onEnable()
    {
        try
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
            instance = new MineCityBukkit(this, config);
            instance.mineCity.dataSource.initDB();
            instance.mineCity.commands.parseXml(MineCity.class.getResourceAsStream("/assets/minecity/commands.xml"));
            instance.mineCity.messageTransformer.parseXML(MineCity.class.getResourceAsStream("/assets/minecity/messages.xml"));

            reloadTask = getScheduler().runTaskTimer(this, instance.mineCity::reloadQueuedChunk, 1, 1);
        }
        catch(Exception e)
        {
            getLogger().log(Level.SEVERE, "Failed to load MineCity, shutting down the server", e);
            Bukkit.shutdown();
        }
    }

    @Override
    public void onDisable()
    {
        try
        {
            instance.mineCity.dataSource.close();
        }
        catch(DataSourceException e)
        {
            e.printStackTrace();
        }
        reloadTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return instance.onCommand(sender, label, args);
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
