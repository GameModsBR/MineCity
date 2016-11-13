package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Arg;
import br.com.gamemods.minecity.api.command.CommandInfo;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.bukkit.command.BukkitPermission;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.economy.EconomyLayer;
import br.com.gamemods.minecity.permission.PermissionLayer;
import br.com.gamemods.minecity.vault.VaultProviders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.Metrics;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MineCityPlugin extends JavaPlugin
{
    private MineCityBukkit instance;
    private BukkitTask reloadTask;
    private BukkitTask playerTick;

    private void adjustDefaultFlag(FileConfiguration yaml, String prefix, PermissionFlag flag, boolean def, SimpleFlagHolder holder)
    {
        boolean allow = yaml.getBoolean(prefix+flag+".allow", def);
        String msg = yaml.getString(prefix+flag+".message", "");

        if(!msg.isEmpty())
            holder.getDefaultMessages().put(flag, Message.string(msg));

        if(!allow)
            holder.deny(flag);
    }

    @Slow
    @Override
    public void onEnable()
    {
        try
        {
            Metrics metrics = new Metrics(this);
            metrics.start();
        }
        catch(Throwable e)
        {
            getLogger().log(Level.WARNING, "MCStats metrics failed to start", e);
        }

        PermissionLayer.register("bukkit", BukkitPermission.PROVIDER);

        if(getServer().getPluginManager().isPluginEnabled("Vault"))
        {
            EconomyLayer.register("vault", VaultProviders.ECONOMY);
            PermissionLayer.register("vault", VaultProviders.PERMISSION);
        }

        BukkitTransformer transformer;
        try
        {
            LegacyFormat.BLACK.server = ChatColor.BLACK;
            LegacyFormat.DARK_BLUE.server = ChatColor.DARK_BLUE;
            LegacyFormat.DARK_GREEN.server = ChatColor.DARK_GREEN;
            LegacyFormat.DARK_AQUA.server = ChatColor.DARK_AQUA;
            LegacyFormat.DARK_RED.server = ChatColor.DARK_RED;
            LegacyFormat.DARK_PURPLE.server = ChatColor.DARK_PURPLE;
            LegacyFormat.GOLD.server = ChatColor.GOLD;
            LegacyFormat.GRAY.server = ChatColor.GRAY;
            LegacyFormat.DARK_GRAY.server = ChatColor.DARK_GRAY;
            LegacyFormat.BLUE.server = ChatColor.BLUE;
            LegacyFormat.GREEN.server = ChatColor.GREEN;
            LegacyFormat.AQUA.server = ChatColor.AQUA;
            LegacyFormat.RED.server = ChatColor.RED;
            LegacyFormat.LIGHT_PURPLE.server = ChatColor.LIGHT_PURPLE;
            LegacyFormat.YELLOW.server = ChatColor.YELLOW;
            LegacyFormat.WHITE.server = ChatColor.WHITE;
            LegacyFormat.RESET.server = ChatColor.RESET;
            LegacyFormat.MAGIC.server = ChatColor.MAGIC;
            LegacyFormat.BOLD.server = ChatColor.BOLD;
            LegacyFormat.STRIKE.server = ChatColor.STRIKETHROUGH;
            LegacyFormat.UNDERLINE.server = ChatColor.UNDERLINE;
            LegacyFormat.ITALIC.server = ChatColor.ITALIC;

            transformer = new SpigotTransformer();
        }
        catch(NoClassDefFoundError ignored)
        {
            transformer = new BukkitTransformer();
        }

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
            config.useTitle = yaml.getBoolean("use-titles", true);
            config.defaultNatureDisableCities = !yaml.getBoolean("permissions.default.nature.enable-city-creation", true);
            config.economy = yaml.getString("economy", "none");
            config.permission = yaml.getString("permissions", "bukkit");

            for(PermissionFlag flag: PermissionFlag.values())
            {
                adjustDefaultFlag(yaml, "permissions.default.nature.", flag, flag.defaultNature, config.defaultNatureFlags);
                adjustDefaultFlag(yaml, "permissions.default.city.", flag, flag.defaultCity, config.defaultCityFlags);
                adjustDefaultFlag(yaml, "permissions.default.plot.", flag, flag.defaultPlot, config.defaultPlotFlags);
                adjustDefaultFlag(yaml, "permissions.default.reserve.", flag, flag.defaultReserve, config.defaultReserveFlags);
            }

            transformer.parseXML(MineCity.class.getResourceAsStream("/assets/minecity/messages-en.xml"));
            String str = config.locale.toLanguageTag();
            if(!str.equals("en"))
            {
                try
                {
                    InputStream resource = MineCity.class.getResourceAsStream("/assets/minecity/messages-"+str +".xml");
                    if(resource != null)
                    {
                        try
                        {
                            transformer.parseXML(resource);
                        }
                        finally
                        {
                            resource.close();
                        }
                    }
                    else
                    {
                        getLogger().log(Level.SEVERE, "There're no translations to "+str+" available.");
                        str = "en";
                    }
                }
                catch(Exception e)
                {
                    getLogger().log(Level.SEVERE, "Failed to load the "+str+" translations", e);
                }
            }
            final String lang = str;

            config.limits.cities = yaml.getInt("limits.cities", -1);
            config.limits.claims = yaml.getInt("limits.claims", -1);
            config.limits.islands = yaml.getInt("limits.islands", -1);

            config.costs.cityCreation = yaml.getDouble("costs.city.creation", 1000);
            config.costs.islandCreation = yaml.getDouble("costs.island.creation", 500);
            config.costs.claim = yaml.getDouble("costs.chunk.claim", 25);

            instance = new MineCityBukkit(this, config, transformer);
            instance.mineCity.dataSource.initDB();
            instance.mineCity.commands.parseXml(MineCity.class.getResourceAsStream("/assets/minecity/commands-"+lang+".xml"));

            Set<String> rootCommands = instance.mineCity.commands.getRootCommands();
            try(InputStream is = MineCityPlugin.class.getResourceAsStream("/plugin.yml"))
            {
                YamlConfiguration plugin = YamlConfiguration.loadConfiguration(new InputStreamReader(is, "UTF-8"));
                ConfigurationSection commands = plugin.getConfigurationSection("commands");
                if(commands != null)
                {
                    commands.getKeys(false).stream().filter(key -> !instance.mineCity.commands.get(key).isPresent())
                            .forEach(key ->
                            {
                                PluginCommand command = getCommand(key);
                                if(command == null)
                                    return;

                                getLogger().info("The command /"+key+" is declared in plugin.yml but is not declared in commands-"+lang+".xml, " +
                                        "trying to unregister it by reflection!"
                                );

                                try
                                {
                                    Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                                    field.setAccessible(true);
                                    SimpleCommandMap commandMap = (SimpleCommandMap) field.get(getServer().getPluginManager());
                                    if(commandMap == null)
                                        throw new NullPointerException();

                                    if(!command.unregister(commandMap))
                                        throw new IllegalStateException("The command has failed to unregister itself");

                                    field = SimpleCommandMap.class.getDeclaredField("knownCommands");
                                    field.setAccessible(true);
                                    Map knownCommands = (Map) field.get(commandMap);
                                    knownCommands.remove(command.getName());
                                    knownCommands.remove("minecity:"+command.getName());
                                    command.getAliases().forEach(s -> {knownCommands.remove("minecity:"+s); knownCommands.remove(s);});

                                    getLogger().info("The command /"+key+" was successfully unregistered");
                                }
                                catch(Exception e)
                                {
                                    getLogger().severe("Failed to unregister the /"+key+" command, it will be present in the game but will do nothing! "
                                            + e.getClass().getSimpleName()+": "+e.getMessage()
                                    );
                                }
                            });
                }
            }


            rootCommands.stream().forEachOrdered(name ->
                    {
                        CommandInfo<?> info = instance.mineCity.commands.get(name).get().command;
                        Command cmd = getCommand(info.getName());
                        if(cmd == null)
                        {
                            getLogger().info("Unable to register the command /" + info.getName() +
                                    " normally because it's not declared in plugin.yml! Attempting to register using reflections");

                            try
                            {
                                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                                field.setAccessible(true);
                                CommandMap commandMap = (CommandMap) field.get(getServer().getPluginManager());
                                ArrayList<String> aliases = new ArrayList<>(info.aliases);
                                aliases.remove(0);
                                commandMap.register("minecity", cmd = new Command(name, "", "", aliases)
                                {
                                    @Override
                                    public boolean execute(CommandSender sender, String commandLabel, String[] args)
                                    {
                                        boolean success;

                                        if(!isEnabled())
                                            return false;

                                        if(!testPermission(sender))
                                            return true;

                                        try
                                        {
                                            success = onCommand(sender, this, commandLabel, args);
                                        }
                                        catch (Throwable ex)
                                        {
                                            throw new CommandException("Unhandled exception executing command '"+commandLabel+
                                                    "' in plugin "+MineCityPlugin.this.getDescription().getFullName(), ex
                                            );
                                        }

                                        if(!success && usageMessage.length() > 0)
                                            for(String line : usageMessage.replace("<command>", commandLabel).split("\n"))
                                                sender.sendMessage(line);

                                        return success;
                                    }
                                });

                                getLogger().info("The command /"+info.getName()+" was successfully registered by reflection");
                            }
                            catch(Exception e)
                            {
                                getLogger().severe("Failed to register the command /"+info.getName()+" using reflections. " +
                                        "The command will not be available. "+e.getClass().getSimpleName()+": "+e.getMessage());
                                return;
                            }
                        }

                        cmd.setDescription(info.description);
                        if(info.args != null && info.args.length > 0)
                        {
                            StringBuilder sb = new StringBuilder();
                            for(Arg arg : info.args)
                            {
                                if(arg.optional())
                                    sb.append('[');
                                else
                                    sb.append('<');
                                sb.append(instance.mineCity.messageTransformer.toSimpleText(new Message(
                                        "cmd."+info.commandId+".arg."+arg.name().toLowerCase().replaceAll("\\s+","-"),
                                        arg.name()
                                )));
                                if(arg.sticky())
                                    sb.append("...");
                                if(arg.optional())
                                    sb.append(']');
                                else
                                    sb.append('>');
                                sb.append(' ');
                            }
                            cmd.setUsage(sb.toString());
                        }
                    }
            );

            List<World> worlds = getServer().getWorlds();
            worlds.stream().map(instance::world).forEachOrdered((DBConsumer<WorldDim>) instance.mineCity::loadNature);
            worlds.stream().map(World::getLoadedChunks).flatMap(Arrays::stream).map(instance::chunk)
                    .forEachOrdered((DBConsumer< ChunkPos>) instance.mineCity::loadChunk);

            getServer().getOnlinePlayers().forEach(instance::player);

            instance.markEntities(worlds.stream().map(World::getLivingEntities).flatMap(List::stream));

            reloadTask = getScheduler().runTaskTimerAsynchronously(this, instance.mineCity::reloadQueuedChunk, 1, 1);
            playerTick = getScheduler().runTaskTimer(this, ()-> instance.playerMap.values().forEach(BukkitPlayer::tick), 1, 1);
            getScheduler().runTaskTimerAsynchronously(this, instance::updateGroups, 1, 1);
        }
        catch(Exception e)
        {
            getLogger().log(Level.SEVERE, "Failed to load MineCity, shutting down the server", e);
            Bukkit.shutdown();
        }
    }

    @Slow
    @Override
    public void onDisable()
    {
        instance.loadingTasks.shutdown();
        try
        {
            instance.loadingTasks.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
            try
            {
                instance.loadingTasks.shutdownNow();
            }
            catch(Exception e2)
            {
                e2.printStackTrace();
            }
        }

        try
        {
            instance.mineCity.dataSource.close();
        }
        catch(DataSourceException e)
        {
            e.printStackTrace();
        }
        if(reloadTask != null)
            reloadTask.cancel();

        if(playerTick != null)
            playerTick.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return instance.onCommand(sender, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        String[] path = new String[args.length+1];
        path[0] = label;
        System.arraycopy(args, 0, path, 1, args.length);
        return instance.mineCity.commands.complete(path);
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
