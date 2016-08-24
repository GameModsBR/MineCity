package br.com.gamemods.minecity.forge.mc_1_7_10;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.RootCommand;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MineCityForgeModTest
{
    public MinecraftServer minecraftServer = mock(MinecraftServer.class);
    private EntityPlayerMP joserobjr = mock(EntityPlayerMP.class);
    public MineCityForgeMod mod = new MineCityForgeMod();
    public CommandHandler commandHandler = mock(CommandHandler.class);
    public MineCity mineCity;

    @Before
    public void setUp() throws Exception
    {
        joserobjr.worldObj = mock(WorldServer.class);
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

        Loader loader = mock(Loader.class);
        field = Loader.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, loader);

        FMLCommonHandler fmlCommonHandler = PowerMockito.mock(FMLCommonHandler.class);
        field = FMLCommonHandler.class.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, fmlCommonHandler);

        when(fmlCommonHandler.bus()).thenReturn(eventBus);

        field = World.class.getDeclaredField("provider");
        field.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(joserobjr.worldObj, mock(WorldProvider.class));
        when(joserobjr.worldObj.provider.getSaveFolder()).thenReturn("Fake");


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
        field = MineCityForgeMod.class.getDeclaredField("config");
        field.setAccessible(true);
        MineCityConfig config = (MineCityConfig) field.get(mod);
        config.dbUrl = "fail-fast";
        try
        {
            mod.onServerStart(new FMLServerAboutToStartEvent(minecraftServer));
        }
        catch(DataSourceException ignored)
        {}
        mineCity = mod.mineCity;
    }

    @Test
    public void testSyncSchedulerLoop() throws Exception
    {
        int[] count = new int[2];
        mod.callSyncMethod(new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(count[0], count[1]++);
                assertNotSame(6, count[1]);
                mod.callSyncMethod(this);
            }
        });

        for(; count[0] < 5; count[0]++)
        {
            TickEvent.ServerTickEvent event = new TickEvent.ServerTickEvent(TickEvent.Phase.START);
            mod.onServerTick(event);
        }

        assertArrayEquals(new int[]{5,5}, count);
    }

    @Test
    @Ignore
    public void testCommandSending() throws Exception
    {
        String xml =
                "<minecity-commands>" +
                        "    <groups>\n" +
                        "        <group id=\"tcity\" cmd=\"tcity,ttown,tc,tt\">\n" +
                        "            <desc>All city related commands</desc>\n" +
                        "            <permission>minecity.cmd.city</permission>\n" +
                        "        </group>\n" +
                        "    </groups>\n" +
                        "    <commands>\n" +
                        "        <command id=\"test.city.create\" parent=\"tcity\" cmd=\"create,new\">\n" +
                        "            <desc>Creates a city</desc>\n" +
                        "            <syntax>name (may have spaces)</syntax>\n" +
                        "            <permission>minecity.cmd.city.create</permission>\n" +
                        "        </command>\n" +
                        "    </commands>\n" +
                        "</minecity-commands>";

        mineCity.commands.parseXml(new ByteArrayInputStream(xml.getBytes()));
        mineCity.commands.registerCommand("test.city.create", null, false, (cmd) -> {
            cmd.sender.send(new Message("test", "Testing "+joserobjr.getCommandSenderName()+": "+cmd.path));
            assertTrue(cmd.sender.isPlayer());
            assertEquals("[tcity, create]", cmd.path.toString());
            assertEquals(Arrays.asList("Test","City"), cmd.args);
            assertEquals(cmd.sender.getPlayerId().uniqueId, joserobjr.getUniqueID());
            assertEquals(cmd.sender.getPlayerId().getName(), joserobjr.getCommandSenderName());
            return CommandResult.success();
        });
        assertEquals("[tcity, create]", mineCity.commands.get("tcity create a b").map(r-> r.path).map(Object::toString).orElse("null"));

        mod.onServerStart(new FMLServerStartingEvent(minecraftServer));

        ArgumentCaptor<RootCommand> cmdCaptor = ArgumentCaptor.forClass(RootCommand.class);
        verify(commandHandler, atLeastOnce()).registerCommand(cmdCaptor.capture());
        RootCommand cmd = cmdCaptor.getAllValues().stream().filter(c->c.name.equals("tcity")).findAny().orElse(null);

        assertEquals("tcity", cmd.name);

        cmd.processCommand(joserobjr, new String[]{"create","Test","City"});

        ArgumentCaptor<ChatComponentText> msgCaptor = ArgumentCaptor.forClass(ChatComponentText.class);
        verify(joserobjr).addChatMessage(msgCaptor.capture());
        ChatComponentText msg = msgCaptor.getValue();
        assertEquals("Testing joserobjr: [tcity, create]", msg.getUnformattedText());
    }
}