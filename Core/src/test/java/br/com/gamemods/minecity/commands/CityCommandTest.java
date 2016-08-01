package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.command.CommandEvent;
import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.datasource.test.TestData;
import br.com.gamemods.minecity.structure.City;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CityCommandTest
{
    private TestData test = new TestData();
    private TestPlayer joserobjr = new TestPlayer(test.joserobjr, new EntityPos(test.overWorld, 440,65,338));
    private CityCommand cmd = new CityCommand(test.mineCity);

    @Test
    public void testCreate() throws Exception
    {
        List<String> path = Arrays.asList("city", "create");
        CommandResult result = cmd.create(new CommandEvent(joserobjr, path, Collections.singletonList("Test City")));
        assertEquals("Error> The chunk that you are standing is not loaded properly", result.toString());

        test.mineCity.loadNature(joserobjr.position.world);
        test.mineCity.loadChunk(joserobjr.position.getChunk());
        result = cmd.create(new CommandEvent(joserobjr, path, Collections.singletonList("Test City")));
        assertTrue(result.success);
        assertNotNull(result.result);
        assertNotNull(result.message);
        assertEquals(City.class, result.result.getClass());
        assertEquals("The city Test City was created successfully, if you get lost you can teleport back with /city spawn testcity",
                result.message.toString());

        CommandResult result2 = cmd.create(new CommandEvent(joserobjr, path, Collections.singletonList("same chunk")));
        assertEquals("Error> The chunk that you are is already claimed to Test City", result2.toString());

        joserobjr.position = joserobjr.position.add(400,0,0);
        test.mineCity.loadChunk(joserobjr.position.getChunk());
        result2 = cmd.create(new CommandEvent(joserobjr, path, Collections.singletonList("test city")));
        assertEquals("Error> The name test city conflicts with Test City", result2.toString());
    }
}