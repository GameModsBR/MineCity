package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.command.Message;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MineCityPluginTest
{
    private Player joserobjr = mock(Player.class);
    private World world = mock(World.class);
    private MineCityPlugin plugin = PowerMockito.mock(MineCityPlugin.class);
    private PluginManager pluginManager = mock(PluginManager.class);
    private MineCityBukkit bukkit;
    private MineCity mineCity;

    @Before
    public void setUp() throws Exception
    {
        when(plugin.getPluginManager()).thenReturn(pluginManager);

        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(world.getName()).thenReturn("world");

        when(joserobjr.getName()).thenReturn("joserobjr");
        when(joserobjr.getUniqueId()).thenReturn(UUID.fromString("185e2176-0095-4ff8-a201-6f2aed9a032a"));
        when(joserobjr.getLocation()).thenReturn(new Location(world, 100,64,250));

        bukkit = new MineCityBukkit(plugin, new MineCityConfig());
        mineCity = bukkit.mineCity;
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
        mineCity.commands.registerCommand("city.create", null, false, (cmd) -> {
            cmd.sender.send(new Message("test", "Testing "+joserobjr.getName()+": "+cmd.path));
            assertTrue(cmd.sender.isPlayer());
            assertEquals("[city, create]", cmd.path.toString());
            assertEquals(Arrays.asList("Test","City"), cmd.args);
            assertEquals(cmd.sender.getPlayerId().uniqueId, joserobjr.getUniqueId());
            assertEquals(cmd.sender.getPlayerId().name, joserobjr.getName());
            return CommandResult.success();
        });
        assertEquals("[city, create]", mineCity.commands.get("city create a b").map(r-> r.path).map(Object::toString).orElse("null"));

        assertTrue(bukkit.onCommand(joserobjr, "city", new String[]{"create","Test","City"}));
        verify(joserobjr).sendMessage("Testing joserobjr: [city, create]");
    }
}
