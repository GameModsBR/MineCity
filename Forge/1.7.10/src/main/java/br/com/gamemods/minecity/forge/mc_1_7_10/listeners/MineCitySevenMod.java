package br.com.gamemods.minecity.forge.mc_1_7_10.listeners;

import br.com.gamemods.minecity.api.MathUtil;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.command.RootCommand;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.SevenTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla.SevenBlockProtections;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla.SevenEntityProtections;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.mcstats.Metrics;
import org.xml.sax.SAXException;

import java.io.IOException;

@Mod(modid = ModEnv.MOD_ID, name = ModEnv.MOD_NAME, version = ModEnv.MOD_VERSION, acceptableRemoteVersions = "*")
public class MineCitySevenMod
{
    private MineCityForge forge;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) throws IOException, SAXException
    {
        MathUtil.sin = MathHelper::sin;
        MathUtil.cos = MathHelper::cos;

        forge = new MineCityForge();
        forge.logger = event.getModLog();
        forge.selectionTool = Items.wooden_hoe;
        forge.selectionPallet = selection -> {
            selection.cornerA = Blocks.glowstone;
            selection.cornerB = Blocks.lit_redstone_lamp;
            selection.corners = Blocks.lit_furnace;
            selection.linesA = Blocks.gold_block;
            selection.linesB = Blocks.lapis_block;
            selection.lines = Blocks.sponge;
            selection.extension = Blocks.glowstone;
        };

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

        forge.onPreInit(new Configuration(event.getSuggestedConfigurationFile()), event.getModLog(), new SevenTransformer());
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

        FMLCommonHandler.instance().bus().register(new SevenTickListener(forge));
        MinecraftForge.EVENT_BUS.register(new SevenToolListener(forge));
        MinecraftForge.EVENT_BUS.register(new SevenWorldListener(forge));
        MinecraftForge.EVENT_BUS.register(new SevenBlockProtections(forge));
        MinecraftForge.EVENT_BUS.register(new SevenEntityProtections(forge));
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
        forge.mineCity.useTitles = false;
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
