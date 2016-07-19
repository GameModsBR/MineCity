package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.commands.CityCommand;
import br.com.gamemods.minecity.commands.TestPlayer;
import br.com.gamemods.minecity.datasource.test.TestData;
import br.com.gamemods.minecity.structure.City;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        tree.register("", "city", new CommandInfo<>(CommandTreeTest::echoCommand), true);
        CommandTree.Result getResult = tree.get("city 1 2 3").orElse(null);
        assertNotNull(getResult);
        assertEquals("[city]",getResult.path.toString());
        assertArrayEquals(new String[]{"1","2","3"}, getResult.args);
        assertEquals("joserobjr: [1, 2, 3]", getResult.run(player).result);

        CityCommand cityCommand = new CityCommand(testData.mineCity);
        tree.register("city", "create", new CommandInfo<>((s,p,a)-> cityCommand.create(s, String.join(" ", a))), false);
        getResult = tree.get("city create").orElse(null);
        assertNotNull(getResult);
        assertArrayEquals(new String[0], getResult.args);

        testData.mineCity.loadNature(player.position.world);
        testData.mineCity.loadChunk(player.position.getChunk());

        CommandResult result = tree.execute(player, "city create");
        assertFalse(result.message.toString(), result.success);
        assertEquals("Error> The name  is not valid, try a bigger name",
                result.toString());

        result = tree.execute(player, "/city create My City");
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
        CommandResult<City> cityResult = tree.execute(player, "/city   create   Spacing    City");
        assertTrue(cityResult.message.toString(), cityResult.success);
        assertEquals("Spacing City", cityResult.result.getName());
    }
}