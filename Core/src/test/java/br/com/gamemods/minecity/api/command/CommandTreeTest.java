package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.commands.CityCommand;
import br.com.gamemods.minecity.commands.TestPlayer;
import br.com.gamemods.minecity.datasource.test.TestData;
import br.com.gamemods.minecity.structure.City;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class CommandTreeTest
{
    private TestData testData = new TestData();

    public static CommandResult<String> echoCommand(CommandSender sender, List<String> path, String[] args)
    {
        return new CommandResult<>(new Message("test","Path: ${path}", new Object[]{"path",path}),
                sender.getPlayerId().name+": "+Arrays.toString(args)
        );
    }

    @Test
    public void testBasic() throws Exception
    {
        TestPlayer player = new TestPlayer(testData.joserobjr, new BlockPos(testData.overWorld, 2,2,3));
        CommandTree tree = new CommandTree();
        tree.register("", new CommandInfo<>("city", CommandTreeTest::echoCommand, "c"), true);
        CommandTree.Result getResult = tree.get("city 1 2 3").orElse(null);
        assertNotNull(getResult);
        assertEquals("[city]",getResult.path.toString());
        assertArrayEquals(new String[]{"1","2","3"}, getResult.args);
        assertEquals("joserobjr: [1, 2, 3]", getResult.run(player).result);

        getResult = tree.get("c 1 2 3").orElse(null);
        assertNotNull(getResult);
        assertEquals("[c]",getResult.path.toString());

        CityCommand cityCommand = new CityCommand(testData.mineCity);
        tree.register("city", new CommandInfo<>("create", cityCommand::create, "c"), false);
        getResult = tree.get("city crEAte 1 2 3").orElse(null);
        assertNotNull(getResult);
        assertEquals("[city, crEAte]", getResult.path.toString());
        assertArrayEquals(new String[]{"1","2","3"}, getResult.args);

        getResult = tree.get("c C").orElse(null);
        assertNotNull(getResult);
        assertEquals("[c, C]", getResult.path.toString());
        assertArrayEquals(new String[0], getResult.args);

        testData.mineCity.loadNature(player.position.world);
        testData.mineCity.loadChunk(player.position.getChunk());

        CommandResult result = tree.invoke(player, "city create");
        assertFalse(result.message.toString(), result.success);
        assertEquals("Error> Please type a city name",
                result.toString());

        result = tree.invoke(player, "city create My City");
        assertTrue(result.success);
        assertNotNull(result.result);
        assertEquals(City.class, result.result.getClass());
        City city = (City) result.result;
        assertEquals("My City", city.getName());
        //noinspection SpellCheckingInspection
        assertEquals("mycity", city.getIdentityName());

        player.position = player.position.add(Direction.EAST, 400);

        testData.mineCity.loadChunk(player.position.getChunk());
        @SuppressWarnings("unchecked")
        CommandResult<City> cityResult = tree.invoke(player, "city   create   Spacing    City");
        assertTrue(cityResult.message.toString(), cityResult.success);
        assertEquals("Spacing City", cityResult.result.getName());
    }

    @Command("city.create")
    public Message testCreateA(CommandSender sender, List<String> path, String[] args)
    {
        return new Message("test", "Path: ${path} Args: ${args}", new Object[][]{
                {"path",path},{"args",Arrays.toString(args)}
        });
    }

    @Test
    public void testRegistration() throws Exception
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

        CommandTree tree = new CommandTree();
        tree.parseXml(new ByteArrayInputStream(xml.getBytes()));
        CommandTree.Result result = tree.get("C creATE A b 2").orElse(null);
        assertNotNull(result);
        assertEquals("[C]", result.path.toString());
        assertArrayEquals(new String[]{"creATE", "A","b","2"}, result.args);

        TestPlayer player = new TestPlayer(testData.joserobjr, new BlockPos(testData.overWorld, 2,2,3));
        CommandResult cmd = tree.invoke(player, "city create test");
        assertFalse(cmd.success);
        assertEquals("Group List: [new, create]", cmd.message.toString());

        tree.registerCommands(this);

        result = tree.get("C creATE A b 2").orElse(null);
        assertNotNull(result);
        assertEquals("[C, creATE]", result.path.toString());
        assertArrayEquals(new String[]{"A","b","2"}, result.args);

        cmd = tree.invoke(player, "city create test");
        assertFalse(cmd.success);
        assertEquals("Path: [city, create] Args: [test]", cmd.message.toString());
    }

    @Test
    public void testAutoComplete() throws Exception
    {
        CommandTree tree = new CommandTree();
        tree.parseXml(CommandTree.class.getResourceAsStream("/assets/minecity/commands.xml"));
        tree.registerCommands(new CityCommand(testData.mineCity));
        assertEquals("[mc, c, create]", tree.get("mc c create a").get().path.toString());
        assertEquals("[c, create]", tree.get("c create a").get().path.toString());

        assertEquals("[city]", tree.complete(new String[]{"cit"}).toString());
        assertEquals("[city, minecity]", tree.complete(new String[]{""}).toString());
        assertEquals("[c, city]", tree.complete(new String[]{"c"}).toString());

        assertEquals("[]", tree.complete(new String[]{"c","map","big","b"}).toString());
        assertEquals("[]", tree.complete(new String[]{"c","map","big",""}).toString());
        assertEquals("[big]", tree.complete(new String[]{"c","map","b"}).toString());
        assertEquals("[big]", tree.complete(new String[]{"c","map",""}).toString());
        assertEquals("[map]", tree.complete(new String[]{"c","map"}).toString());
        tree.cityNames = ()-> Stream.of("Name With Spaces");

        assertEquals("[NameWithSpaces, Name]", tree.complete(new String[]{"c","spawn","name"}).toString());
        assertEquals("[WithSpaces, With]", tree.complete(new String[]{"c","spawn","name", ""}).toString());
        assertEquals("[WithSpaces, With]", tree.complete(new String[]{"c","spawn","name", "wit"}).toString());
        assertEquals("[]", tree.complete(new String[]{"c","spawn","name", "wit", ""}).toString());
        assertEquals("[Spaces]", tree.complete(new String[]{"c","spawn","name", "with", ""}).toString());
        assertEquals("[]", tree.complete(new String[]{"c","spawn","name", "wit", "s"}).toString());
        assertEquals("[Spaces]", tree.complete(new String[]{"c","spawn","name", "with", "s"}).toString());
        assertEquals("[]", tree.complete(new String[]{"c","spawn","name", "with", "k"}).toString());
    }
}