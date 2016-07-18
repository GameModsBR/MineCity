package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.datasource.test.TestData;
import br.com.gamemods.minecity.structure.City;
import static com.github.kolorobot.exceptions.java8.AssertJThrowableAssert.assertThrown;
import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.internal.matchers.Contains;

public class CityCommandTest
{
    private TestData test = new TestData();
    private TestPlayer joserobjr = new TestPlayer(test.joserobjr, new BlockPos(test.overWorld, 440,65,338));
    private CityCommand cmd = new CityCommand(test.mineCity);

    @Test
    public void testCreate() throws Exception
    {
        CommandResult result = cmd.create(joserobjr, "Test City");
        assertEquals("Error> The chunk that you are standing is not loaded properly", result.toString());

        test.mineCity.loadNature(joserobjr.position.world);
        test.mineCity.loadChunk(joserobjr.position.getChunk());
        result = cmd.create(joserobjr, "Test City");
        assertTrue(result.success);
        assertNotNull(result.result);
        assertNotNull(result.message);
        assertEquals(City.class, result.result.getClass());
        assertEquals("The city Test City was created successfully, if you get lost you can teleport back with /city spawn testcity",
                result.message.toString());
    }
}