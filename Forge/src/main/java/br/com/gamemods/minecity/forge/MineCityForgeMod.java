package br.com.gamemods.minecity.forge;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.command.ForgePlayer;
import br.com.gamemods.minecity.forge.command.RootCommand;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Mod(modid = "minecity", name = "MineCity", version = "1.0-SNAPSHOT", acceptableRemoteVersions = "*")
public class MineCityForgeMod
{
    public MineCity mineCity;
    private MineCityConfig config;
    private Path worldContainer;

    @EventHandler
    @SideOnly(Side.SERVER)
    public void onPreInit(FMLPreInitializationEvent event)
    {
        config = new MineCityConfig();
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public void onServerStart(FMLServerAboutToStartEvent event)
    {
        MinecraftServer server = event.getServer();
        worldContainer = Paths.get(server.getFolderName());

        MinecraftForge.EVENT_BUS.register(this);
        mineCity = new MineCity(config);
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public void onServerStart(FMLServerStartingEvent event)
    {
        mineCity.commands.getRootCommands().stream()
                .map(name->mineCity.commands.get(name).get())
                .map(r->r.command).distinct()
                .forEach(i-> event.registerServerCommand(new RootCommand<>(this, i)));
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public void onServerStop(FMLServerStoppedEvent event)
    {
        MinecraftForge.EVENT_BUS.unregister(this);
        mineCity = null;
        worldContainer = null;
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkEvent.Load event) throws DataSourceException
    {
        Chunk chunk = event.getChunk();
        mineCity.loadChunk(new ChunkPos(world(event.world), chunk.xPosition, chunk.zPosition));
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkEvent.Unload event) throws DataSourceException
    {
        Chunk chunk = event.getChunk();
        mineCity.unloadChunk(new ChunkPos(world(event.world), chunk.xPosition, chunk.zPosition));
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldEvent.Load event) throws DataSourceException
    {
        mineCity.loadNature(world(event.world));
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldUnload(WorldEvent.Unload event) throws DataSourceException
    {
        mineCity.unloadNature(world(event.world));
    }

    @SideOnly(Side.SERVER)
    public ChunkPos chunk(Chunk chunk)
    {
        return new ChunkPos(world(chunk.worldObj), chunk.xPosition, chunk.zPosition);
    }

    @SideOnly(Side.SERVER)
    public WorldDim world(World world)
    {
        Path worldPath = worldContainer.resolve(Optional.ofNullable(world.provider.getSaveFolder()).orElse(""));
        return new WorldDim(world.provider.dimensionId, worldPath.toString());
    }

    public CommandSender sender(ICommandSender sender)
    {
        if(sender instanceof EntityPlayer)
            return new ForgePlayer(this, (EntityPlayer) sender);
        return new ForgeCommandSender<>(this, sender);
    }
}
