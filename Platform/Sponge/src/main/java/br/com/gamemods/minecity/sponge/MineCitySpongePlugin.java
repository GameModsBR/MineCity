package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.permission.SimpleFlagHolder;
import br.com.gamemods.minecity.api.unchecked.UncheckedException;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.economy.EconomyLayer;
import br.com.gamemods.minecity.permission.PermissionLayer;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.script.ScriptEngine;
import br.com.gamemods.minecity.sponge.cmd.SpongeRootCommand;
import br.com.gamemods.minecity.sponge.cmd.SpongeTransformer;
import br.com.gamemods.minecity.sponge.core.mixed.MixedBlockType;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import br.com.gamemods.minecity.sponge.listeners.ActionListener;
import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Chunk;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Plugin(id="minecity", name="MineCity", version = "@VERSION@", authors = "joserobjr", description = "@DESC@")
public class MineCitySpongePlugin
{
    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private MineCityConfig config;
    private SpongeTransformer transformer;
    private String lang;
    private MineCitySponge sponge;
    private Task reloadTask;
    private Task playerTickTask;
    private ScriptEngine engine;

    @Listener
    public void onGameConstruct(GameConstructionEvent event)
    {
        try
        {
            LegacyFormat.BLACK.server = TextColors.BLACK;
            LegacyFormat.DARK_BLUE.server = TextColors.DARK_BLUE;
            LegacyFormat.DARK_GREEN.server = TextColors.DARK_GREEN;
            LegacyFormat.DARK_AQUA.server = TextColors.DARK_AQUA;
            LegacyFormat.DARK_RED.server = TextColors.DARK_RED;
            LegacyFormat.DARK_PURPLE.server = TextColors.DARK_PURPLE;
            LegacyFormat.GOLD.server = TextColors.GOLD;
            LegacyFormat.GRAY.server = TextColors.GRAY;
            LegacyFormat.DARK_GRAY.server = TextColors.DARK_GRAY;
            LegacyFormat.BLUE.server = TextColors.BLUE;
            LegacyFormat.GREEN.server = TextColors.GREEN;
            LegacyFormat.AQUA.server = TextColors.AQUA;
            LegacyFormat.RED.server = TextColors.RED;
            LegacyFormat.LIGHT_PURPLE.server = TextColors.LIGHT_PURPLE;
            LegacyFormat.YELLOW.server = TextColors.YELLOW;
            LegacyFormat.WHITE.server = TextColors.WHITE;
            LegacyFormat.RESET.server = TextColors.RESET;
            LegacyFormat.MAGIC.server = TextStyles.OBFUSCATED;
            LegacyFormat.BOLD.server = TextStyles.BOLD;
            LegacyFormat.STRIKE.server = TextStyles.STRIKETHROUGH;
            LegacyFormat.UNDERLINE.server = TextStyles.UNDERLINE;
            LegacyFormat.ITALIC.server = TextStyles.ITALIC;

            Hand.MAIN.setInstance(HandTypes.MAIN_HAND);
            Hand.OFF.setInstance(HandTypes.OFF_HAND);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Sponge.getServer().shutdown();
            throw e;
        }
    }

    private double getOrSetDouble(ConfigurationNode node, double def)
    {
        return getOrSet(node, def, node::getDouble);
    }

    private int getOrSetInt(ConfigurationNode node, int def)
    {
        return getOrSet(node, def, node::getInt);
    }

    private String getOrSetStr(ConfigurationNode node, String def)
    {
        return getOrSet(node, def, node::getString);
    }

    private boolean getOrSetBool(ConfigurationNode node, boolean def)
    {
        return getOrSet(node, def, node::getBoolean);
    }

    private <T> T getOrSet(ConfigurationNode node, T def, Supplier<T> getter)
    {
        T obj = getter.get();
        if(node.isVirtual() || obj == null)
        {
            node.setValue(def);
            return def;
        }

        return obj;
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event) throws IOException, SAXException
    {
        CommentedConfigurationNode root = configManager.load();
        try
        {
            PermissionLayer.register("sponge", SpongeProviders.PERMISSION);
            EconomyLayer.register("sponge", SpongeProviders.ECONOMY);

            CommentedConfigurationNode dbConfig = root.getNode("database");
            MineCityConfig config = new MineCityConfig();
            config.dbUrl = getOrSetStr(dbConfig.getNode("url"), config.dbUrl);
            config.dbUser = Optional.ofNullable(getOrSetStr(dbConfig.getNode("user"), "")).filter(u-> !u.isEmpty()).orElse(null);
            config.dbPass = Optional.ofNullable(getOrSetStr(dbConfig.getNode("pass"), "")).filter(p-> !p.isEmpty()).map(String::getBytes).orElse(null);
            config.locale = Locale.forLanguageTag(Optional.ofNullable(getOrSetStr(root.getNode("general", "language"), "en")).filter(l->!l.isEmpty()).orElse("en"));
            config.useTitle = getOrSetBool(root.getNode("general", "use-titles"), true);

            CommentedConfigurationNode permsConfig = root.getNode("permissions", "default");
            config.defaultNatureDisableCities = getOrSetBool(permsConfig.getNode("nature", "enable-city-creation"), true);
            config.economy = getOrSetStr(root.getNode("manager", "economy"), "none");
            config.permission = getOrSetStr(root.getNode("manager", "permissions"), "sponge");

            for(PermissionFlag flag: PermissionFlag.values())
            {
                adjustDefaultFlag(permsConfig.getNode("nature", flag.name()), flag, flag.defaultNature, config.defaultNatureFlags);
                adjustDefaultFlag(permsConfig.getNode("city", flag.name()), flag, flag.defaultCity, config.defaultCityFlags);
                adjustDefaultFlag(permsConfig.getNode("plot", flag.name()), flag, flag.defaultPlot, config.defaultPlotFlags);
                adjustDefaultFlag(permsConfig.getNode("reserve", flag.name()), flag, flag.defaultReserve, config.defaultReserveFlags);
            }

            transformer = new SpongeTransformer();
            transformer.parseXML(MineCity.class.getResourceAsStream("/assets/minecity/messages-en.xml"));
            lang = config.locale.toLanguageTag();
            if(!lang.equals("en"))
            {
                try
                {
                    InputStream resource = MineCity.class.getResourceAsStream("/assets/minecity/messages-"+lang +".xml");
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
                        logger.error("There're no translations to "+lang+" available.");
                        lang = "en";
                    }
                }
                catch(Exception e)
                {
                    logger.error("Failed to load the "+lang+" translations", e);
                }
            }

            CommentedConfigurationNode limits = root.getNode("limits");
            config.limits.cities = getOrSetInt(limits.getNode("cities"), -1);
            config.limits.claims = getOrSetInt(limits.getNode("claims"), -1);
            config.limits.islands = getOrSetInt(limits.getNode("islands"), -1);

            CommentedConfigurationNode costs = root.getNode("costs");
            config.costs.cityCreation = getOrSetDouble(costs.getNode("city", "creation"), 1000);
            config.costs.islandCreation = getOrSetDouble(costs.getNode("island", "creation"), 500);
            config.costs.claim = getOrSetDouble(costs.getNode("chunk", "claim"), 25);
            this.config = config;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Sponge.getServer().shutdown();
            throw e;
        }
        finally
        {
            configManager.save(root);
        }
    }

    private void adjustDefaultFlag(CommentedConfigurationNode node, PermissionFlag flag, boolean def, SimpleFlagHolder holder)
    {
        boolean allow = getOrSetBool(node.getNode("allow"), def);
        String msg = getOrSetStr(node.getNode("message"), "");

        if(!msg.isEmpty())
            holder.getDefaultMessages().put(flag, Message.string(msg));

        if(!allow)
            holder.deny(flag);
    }

    @Command("reactive.reload")
    public CommandResult<?> reloadReactions(CommandEvent cmd)
    {
        SpongeManipulator manipulator = new SpongeManipulator(sponge);
        ReactiveLayer.setManipulator(manipulator);
        ReactiveLayer.setReactor(manipulator);
        Sponge.getGame().getRegistry().getAllOf(BlockType.class).forEach(type-> {
            ReactiveLayer.getBlockType(type).get().setReactive(null);
            if(type instanceof MixedBlockType)
                ((MixedBlockType) type).setBlockTypeData(null);
        });
        loadScripts();
        return CommandResult.success();
    }

    @Command("dump.blocks")
    public CommandResult<?> dumpBlocks(CommandEvent cmd) throws IOException
    {
        TreeMap<String, String> roleMap = new TreeMap<>();
        Sponge.getGame().getRegistry().getAllOf(BlockType.class).forEach(type-> {
            String id = type.getId();
            roleMap.put(id, ReactiveLayer.getBlockType(type).flatMap(BlockTypeData::getReactiveBlockType).map(it-> it.getBlockRole()+":"+it.getClass().getName()).orElse(null));
            type.getTraits().forEach(trait-> roleMap.put(id+":"+trait.getId(), ReactiveLayer.getBlockTrait(trait).flatMap(BlockTraitData::getReactiveBlockTrait).map(it-> it.getClass().getName()).orElse(null)));
        });

        try(FileWriter fw = new FileWriter(configDir.resolve("dump_blocks.txt").toFile()); BufferedWriter out = new BufferedWriter(fw))
        {
            out.write(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date()));
            out.newLine();
            for(Map.Entry<String, String> entry : roleMap.entrySet())
            {
                out.write(entry.getKey()+": "+entry.getValue());
                out.newLine();
            }
        }
        return CommandResult.success();
    }

    private void loadScripts()
    {
        Sponge.getPluginManager().getPlugins().forEach(plugin ->
                {
                    try
                    {
                        engine.load(plugin.getId());
                    }
                    catch(ResourceException e)
                    {
                        logger.warn("No reactive definition was found for "+plugin.getId()+": "+e);
                    }
                    catch(ScriptException e)
                    {
                        logger.error("An error has occurred while loading "+plugin.getId()+"'s reactive definitions", e);
                        throw new UncheckedException(e);
                    }
                }
        );
    }

    @Listener
    public void onGameServerAboutToStart(GameAboutToStartServerEvent event) throws DataSourceException, SAXException, IOException
    {
        try
        {
            sponge = new MineCitySponge(this, config, transformer, logger);
            SpongeManipulator manipulator = new SpongeManipulator(sponge);
            ReactiveLayer.setManipulator(manipulator);
            ReactiveLayer.setReactor(manipulator);
            sponge.mineCity.commands.registerCommands(this);

            Sponge.getEventManager().registerListeners(this, new ActionListener(sponge));

            Path scripts = configDir.resolve("scripts");
            File scriptsDir = scripts.toFile();
            if(!scriptsDir.isDirectory() && !scriptsDir.mkdirs())
                logger.warn("Failed to create the directory: "+scriptsDir);

            engine = new ScriptEngine(
                    scripts.toUri().toURL(),
                    ReactiveLayer.class.getResource("/minecity/scripts/"),
                    ReactiveLayer.class.getResource("/minecity/scripts/minecity/")
            );

            loadScripts();

            sponge.mineCity.dataSource.initDB();
            sponge.mineCity.commands.parseXml(MineCity.class.getResourceAsStream("/assets/minecity/commands-"+lang+".xml"));

            sponge.mineCity.commands.getRootCommands().forEach(name->
                    Sponge.getCommandManager().register(this, new SpongeRootCommand(sponge, name), name)
            );

            reloadTask = Sponge.getScheduler().createTaskBuilder()
                    .async()
                    .execute(sponge.mineCity::reloadQueuedChunk)
                    .intervalTicks(1)
                    .delayTicks(1)
                    .submit(this);

            playerTickTask =Sponge.getScheduler().createTaskBuilder()
                    .execute(()-> Sponge.getServer().getOnlinePlayers().forEach(player -> sponge.player(player).tick()))
                    .intervalTicks(1)
                    .submit(this);
        }
        catch(Exception e)
        {
            logger.error("Failed to load MineCity, shutting down the server", e);
            Sponge.getServer().shutdown();
            throw e;
        }
    }

    @Listener
    public void onGameServerStopped(GameStoppedServerEvent event)
    {
        if(sponge != null)
        {
            sponge.loadingTasks.shutdown();
            try
            {
                sponge.loadingTasks.awaitTermination(5, TimeUnit.SECONDS);
            }
            catch(InterruptedException e)
            {
                logger.error("Failed to wait the loading tasks completes", e);
                try
                {
                    sponge.loadingTasks.shutdownNow();
                }
                catch(Exception e2)
                {
                    logger.error("Failed to shutdown the loading tasks", e2);
                }
            }

            try
            {
                sponge.mineCity.dataSource.close();
            }
            catch(DataSourceException e)
            {
                logger.error("Failed to close the dataSource", e);
            }
        }

        if(reloadTask != null)
            reloadTask.cancel();
    }

    @Listener(order = Order.POST)
    public void onWorldLoad(LoadWorldEvent event)
    {
        //logger.trace("WLA:"+event.getTargetWorld().getName());
        sponge.loadingTasks.submit(()->
        {
            //logger.debug("WLB:"+event.getTargetWorld().getName());
            sponge.world(event.getTargetWorld());
        });
    }

    @Listener(order = Order.POST)
    public void onWorldUnload(UnloadWorldEvent event)
    {
        //logger.trace("WUA:"+event.getTargetWorld().getName());
        sponge.loadingTasks.submit(()->
        {
            //logger.debug("WUB:"+event.getTargetWorld().getName());
            sponge.mineCity.unloadNature(sponge.world(event.getTargetWorld()));
        });
    }

    @Listener(order = Order.PRE)
    public void onChunkLoadPre(LoadChunkEvent event)
    {
        Chunk chunk = event.getTargetChunk();
        //logger.info("CLPA:"+ReactiveLayer.getReactiveChunk(chunk));
        Vector3i pos = chunk.getPosition();
        //logger.info("CLPB:"+(chunk instanceof MixedChunk));
    }

    @Listener(order = Order.POST)
    public void onChunkLoad(LoadChunkEvent event)
    {
        Chunk chunk = event.getTargetChunk();
        //logger.trace("CLA:"+chunk);
        sponge.loadingTasks.submit(()->
        {
            //logger.debug("CLB:"+chunk);
            try
            {
                sponge.mineCity.loadChunk(sponge.chunk(chunk));
            }
            catch(Exception e)
            {
                Vector3i position = chunk.getPosition();
                logger.error(
                        "Failed to load the chunk "+
                                chunk.getWorld().getName()+" "+position.getX()+" "+position.getY()+" "+position.getZ(),
                        e
                );
            }
        });
    }

    @Listener(order = Order.POST)
    public void onChunkUnload(UnloadChunkEvent event)
    {
        //logger.trace("CUA:"+event.getTargetChunk()+"");
        sponge.loadingTasks.submit(()-> {
            //logger.debug("CUB:"+event.getTargetChunk()+"");
            sponge.mineCity.unloadChunk(sponge.chunk(event.getTargetChunk()));
        });
    }

    @Listener(order = Order.PRE)
    public void onEntitySpawn(SpawnEntityEvent event)
    {
        event.getEntities().forEach(ReactiveLayer::getEntityData);
    }

    @Override
    public String toString()
    {
        return "MineCitySpongePlugin{"+
                "configDir="+configDir+
                ", lang='"+lang+'\''+
                ", sponge="+sponge+
                '}';
    }
}
