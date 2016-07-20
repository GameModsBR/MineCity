package br.com.gamemods.minecity.forge;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.command.RootCommand;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MineCityForgeModTest
{
    public MinecraftServer minecraftServer = mock(MinecraftServer.class);
    private EntityPlayer joserobjr = mock(EntityPlayer.class);
    public MineCityForgeMod mod = new MineCityForgeMod();
    public CommandHandler commandHandler = mock(CommandHandler.class);
    public MineCity mineCity;

    @Before
    public void setUp() throws Exception
    {
        when(minecraftServer.getFolderName()).thenReturn("world");
        when(minecraftServer.getCommandManager()).thenReturn(commandHandler);

        when(joserobjr.getCommandSenderName()).thenReturn("joserobjr");
        when(joserobjr.getUniqueID()).thenReturn(UUID.fromString("185e2176-0095-4ff8-a201-6f2aed9a032a"));

        EventBus eventBus = mock(EventBus.class);
        Field field = MinecraftForge.class.getDeclaredField("EVENT_BUS");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, eventBus);

        FMLPreInitializationEvent event = mock(FMLPreInitializationEvent.class);
        File file = new File("build/test-unit/config.cfg");
        if(!file.getParentFile().isDirectory())
            assertTrue(file.getParentFile().mkdirs());

        field = FMLInjectionData.class.getDeclaredField("minecraftHome");
        field.setAccessible(true);
        field.set(null, file.getParentFile());
        field.setAccessible(false);

        when(event.getSuggestedConfigurationFile()).thenReturn(file);
        mod.onPreInit(event);
        mod.onServerStart(new FMLServerAboutToStartEvent(minecraftServer));
        mineCity = mod.mineCity;
    }

    @Test
    public void testCommandSending() throws Exception
    {
        String xml =
                "<minecity-commands>" +
                        "    <groups>\n" +
                        "        <group id=\"city\" cmd=\"city,town,c,t\">\n" +
                        "            <desc>All city related commands</desc>\n" +
                        "            <permission>minecity.cmd.city</permission>\n" +
                        "        </group>\n" +
                        "    </groups>\n" +
                        "    <commands>\n" +
                        "        <command id=\"city.create\" parent=\"city\" cmd=\"create,new\">\n" +
                        "            <desc>Creates a city</desc>\n" +
                        "            <syntax>name (may have spaces)</syntax>\n" +
                        "            <permission>minecity.cmd.city.create</permission>\n" +
                        "        </command>\n" +
                        "    </commands>\n" +
                        "</minecity-commands>";

        mineCity.commands.parseXml(new ByteArrayInputStream(xml.getBytes()));
        mineCity.commands.registerCommand("city.create", (sender, path, args) -> {
            sender.send(new Message("test", "Testing "+joserobjr.getCommandSenderName()+": "+path));
            assertTrue(sender.isPlayer());
            assertEquals("[city, create]", path.toString());
            assertArrayEquals(new String[]{"Test","City"}, args);
            assertEquals(sender.getPlayerId().uniqueId, joserobjr.getUniqueID());
            assertEquals(sender.getPlayerId().name, joserobjr.getCommandSenderName());
            return CommandResult.success();
        });
        assertEquals("[city, create]", mineCity.commands.get("city create a b").map(r-> r.path).map(Object::toString).orElse("null"));

        mod.onServerStart(new FMLServerStartingEvent(minecraftServer));

        ArgumentCaptor<RootCommand> cmdCaptor = ArgumentCaptor.forClass(RootCommand.class);
        verify(commandHandler).registerCommand(cmdCaptor.capture());
        RootCommand cmd = cmdCaptor.getValue();

        assertEquals("city", cmd.name);

        cmd.processCommand(joserobjr, new String[]{"create","Test","City"});

        ArgumentCaptor<ChatComponentText> msgCaptor = ArgumentCaptor.forClass(ChatComponentText.class);
        verify(joserobjr).addChatMessage(msgCaptor.capture());
        ChatComponentText msg = msgCaptor.getValue();
        assertEquals("Testing joserobjr: [city, create]", msg.getUnformattedTextForChat());
    }
}