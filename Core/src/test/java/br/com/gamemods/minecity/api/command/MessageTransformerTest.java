package br.com.gamemods.minecity.api.command;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import static br.com.gamemods.minecity.api.command.LegacyFormat.*;
import static org.junit.Assert.assertEquals;

public class MessageTransformerTest
{
    MessageTransformer transformer;
    @Before
    public void setUp() throws Exception
    {
        transformer = new MessageTransformer();
        transformer.parseXML(getClass().getResourceAsStream("/assets/minecity/test-messages.xml"));
    }

    @Test
    public void testLegacy() throws Exception
    {
        Element element = transformer.getElement("test.bold").get();
        assertEquals("bold", element.getAttribute("id"));

        Message message = new Message("test.bold");
        assertEquals("This message has a bold word", transformer.toSimpleText(message));

        assertEquals("A "+BOLD+"bold"+RESET+" word", transformer.toLegacy("<msg>A <b>bold</b> word</msg>"));
        assertEquals("Has "+BOLD+"bold and "+RESET+BOLD+ITALIC+"italic"+RESET+" words",
                transformer.toLegacy("<msg>Has <b>bold and <i>italic</i></b> words</msg>"));

        assertEquals("Has "+BOLD+"bold and "+RESET+BOLD+ITALIC+"italic"+RESET+BOLD+" words",
                transformer.toLegacy("<msg>Has <b>bold and <i>italic</i> words</b></msg>"));

        assertEquals(BOLD+"Bold"+RESET+" spaces", transformer.toLegacy("<msg><b>Bold</b>     spaces</msg>"));
        assertEquals(BOLD+"Bold"+RESET+"     spaces", transformer.toLegacy("<msg><b>Bold</b><![CDATA[     spaces]]></msg>"));
        assertEquals("A "+BOLD+"Bold"+RESET+" word", transformer.toLegacy("<msg>\n\tA\n\t<b>Bold</b>\n\tword\n</msg>"));
    }
}