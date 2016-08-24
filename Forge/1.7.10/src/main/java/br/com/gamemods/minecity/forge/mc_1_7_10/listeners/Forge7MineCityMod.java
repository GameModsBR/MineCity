package br.com.gamemods.minecity.forge.mc_1_7_10.listeners;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.ModConstants;
import br.com.gamemods.minecity.forge.base.command.RootCommand;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCityForge7;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.Forge7Transformer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.mcstats.Metrics;
import org.xml.sax.SAXException;

import java.io.IOException;

@Mod(modid = ModConstants.MOD_ID, name = ModConstants.MOD_NAME, version = ModConstants.MOD_VERSION, acceptableRemoteVersions = "*")
public class Forge7MineCityMod
{
    private MineCityForge forge;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) throws IOException, SAXException
    {
        forge = new MineCityForge7();

        LegacyFormat.BLACK.server = EnumChatFormatting.BLACK;
        LegacyFormat.DARK_BLUE.server = EnumChatFormatting.DARK_BLUE;
        LegacyFormat.DARK_GREEN.server = EnumChatFormatting.DARK_GREEN;
        LegacyFormat.DARK_AQUA.server = EnumChatFormatting.DARK_AQUA;
        LegacyFormat.DARK_RED.server = EnumChatFormatting.DARK_RED;
        LegacyFormat.DARK_PURPLE.server = EnumChatFormatting.DARK_PURPLE;
        LegacyFormat.GOLD.server = EnumChatFormatting.GOLD;
        LegacyFormat.GRAY.server = EnumChatFormatting.GRAY;
        LegacyFormat.DARK_GRAY.server = EnumChatFormatting.DARK_GRAY;
        LegacyFormat.BLUE.server = EnumChatFormatting.BLUE;
        LegacyFormat.GREEN.server = EnumChatFormatting.GREEN;
        LegacyFormat.AQUA.server = EnumChatFormatting.AQUA;
        LegacyFormat.RED.server = EnumChatFormatting.RED;
        LegacyFormat.LIGHT_PURPLE.server = EnumChatFormatting.LIGHT_PURPLE;
        LegacyFormat.YELLOW.server = EnumChatFormatting.YELLOW;
        LegacyFormat.WHITE.server = EnumChatFormatting.WHITE;
        LegacyFormat.RESET.server = EnumChatFormatting.RESET;
        LegacyFormat.MAGIC.server = EnumChatFormatting.OBFUSCATED;
        LegacyFormat.BOLD.server = EnumChatFormatting.BOLD;
        LegacyFormat.STRIKE.server = EnumChatFormatting.STRIKETHROUGH;
        LegacyFormat.UNDERLINE.server = EnumChatFormatting.UNDERLINE;
        LegacyFormat.ITALIC.server = EnumChatFormatting.ITALIC;

        forge.onPreInit(new Configuration(event.getSuggestedConfigurationFile()), event.getModLog(), new Forge7Transformer());
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event)
    {
        try
        {
            Metrics metrics = new Metrics("MineCity", "forge-1.0-SNAPSHOT");
            metrics.start();
        }
        catch(Throwable e)
        {
            forge.logger.warn("MCStats metrics failed to start", e);
        }

        FMLCommonHandler.instance().bus().register(new Forge7TickListener(forge));
        MinecraftForge.EVENT_BUS.register(new Forge7ToolListener(forge));
        MinecraftForge.EVENT_BUS.register(new Forge7WorldListener(forge));
    }

    @Slow
    @Mod.EventHandler
    public void onServerStart(FMLServerAboutToStartEvent event) throws IOException, DataSourceException, SAXException
    {
        forge.onServerAboutToStart(event.getServer());
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        forge.mineCity.commands.getRootCommands().stream()
                .map(name->forge.mineCity.commands.get(name).get())
                .map(r->r.command).distinct()
                .forEach(i-> event.registerServerCommand(new RootCommand<>(forge, i)));
    }

    @Slow
    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) throws DataSourceException
    {
        forge.onServerStop();
    }
}