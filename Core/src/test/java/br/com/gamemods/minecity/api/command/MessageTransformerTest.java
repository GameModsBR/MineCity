package br.com.gamemods.minecity.api.command;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

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
    public void testComponent() throws Exception
    {
        MessageTransformer.Component component = transformer.parse("abc");
        assert component instanceof MessageTransformer.TextComponent;
        assertEquals("abc", ((MessageTransformer.TextComponent) component).text);
        assertEquals(Collections.emptyList(), component.extra);

        component = transformer.parse("normal"+RED+"red "+RESET+"normal "+BOLD+"bold "+GREEN+"bold and green "+RESET+"normal "+GREEN+"green "+BOLD+"green and bold");
        assert component instanceof MessageTransformer.TextComponent;
        assertEquals("normal", ((MessageTransformer.TextComponent) component).text);
        assertEquals("["+MARK+"cred , normal , "+MARK+"lbold , "+MARK+"a"+MARK
                +"lbold and green , normal , "+MARK+"agreen , "+MARK+"a"+MARK+"lgreen and bold]",
                component.extra.toString()
        );

        component = transformer.parse("<msg>normal1<red>red </red>normal2 <b>bold <green>bold and green </green></b>normal3 <green>green2 <b>green and bold</b></green></msg>");
        String legacy = component.toString();
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("normal1")));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("normal2")));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("normal3")));
        assertEquals(legacy, EnumSet.of(RED), LegacyFormat.formatAt(legacy, legacy.indexOf("red ")));
        assertEquals(legacy, EnumSet.of(RESET, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("bold ")));
        assertEquals(legacy, EnumSet.of(GREEN, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("bold and green ")));
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf("green2 ")));
        assertEquals(legacy, EnumSet.of(GREEN, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("green and bold")));

        component = transformer.parse("A message with ${token}");
        assertEquals("A message with ", ((MessageTransformer.TextComponent) component).text);

        MessageTransformer.Component clone = component.clone();
        clone.apply(Locale.US, new Object[][]{{"token","a token"}});
        assertEquals("A message with a token", clone.toString());

        component = transformer.parse("<msg><red>red <b>${token}</b> red</red></msg>");
        legacy = component.toString();
        assertEquals(legacy, EnumSet.of(RED), LegacyFormat.formatAt(legacy, legacy.indexOf("red ")));
        assertEquals(legacy, EnumSet.of(RED, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("${token}")));
        assertEquals(legacy, EnumSet.of(RED), LegacyFormat.formatAt(legacy, legacy.indexOf(" ")));

        clone = component.clone();
        clone.apply(Locale.US, new Object[][]{{"token",new Message("","<msg>red and bold <green>bold and green</green></msg>")}});
        legacy = clone.toString();
        assertEquals(legacy, EnumSet.of(RED, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("red and bold ")));
        assertEquals(legacy, EnumSet.of(GREEN, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("bold and green")));
        assertEquals(legacy, EnumSet.of(RED), LegacyFormat.formatAt(legacy, legacy.indexOf("red ")));
        assertEquals(legacy, EnumSet.of(RED), LegacyFormat.formatAt(legacy, legacy.indexOf(" red")));
    }

    @Test @Ignore
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

    @Test @Ignore
    public void testInline() throws Exception
    {
        Message inline = new Message("", "inline");
        Message container = new Message("", "This contains an ${msg} message", new Object[]{"msg", inline});
        assertEquals("This contains an inline message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", RED+"inline");
        container = new Message("", "This contains an ${msg} message", new Object[]{"msg", inline});
        assertEquals("This contains an "+RED+"inline"+RESET+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", RED+"inline");
        container = new Message("", GREEN+"This contains an ${msg} message", new Object[]{"msg", inline});
        assertEquals(GREEN+"This contains an "+RED+"inline"+GREEN+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        container = new Message("", GREEN+"This contains an "+BOLD+"${msg} message", new Object[]{"msg", inline});
        assertEquals(GREEN+"This contains an "+BOLD+RED+BOLD+"inline"+GREEN+BOLD+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", "<msg>inline</msg>");
        container = new Message("", "<msg>This contains an ${msg} message</msg>", new Object[]{"msg", inline});
        assertEquals("This contains an inline message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", "<msg><red>inline</red></msg>");
        container = new Message("", "<msg>This contains an ${msg} message</msg>", new Object[]{"msg", inline});
        assertEquals("This contains an "+RED+"inline"+RESET+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", "<msg><red>inline</red></msg>");
        container = new Message("", "<msg><green>This contains an ${msg} message</green></msg>", new Object[]{"msg", inline});
        assertEquals(GREEN+"This contains an "+GREEN+RED+"inline"+GREEN+" message", transformer.toLegacy(container));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        container = new Message("", "<msg><green>This contains an <b>${msg} message</b></green></msg>", new Object[]{"msg", inline});
        String result = transformer.toLegacy(container);
        assertEquals(GREEN+"This contains an "+RESET+GREEN+BOLD+GREEN+BOLD+RED+BOLD+BOLD+"inline"+GREEN+BOLD+" message",result);
        assertEquals("This contains an inline message", transformer.toSimpleText(container));
    }

    @Test @Ignore
    public void testSkip() throws Exception
    {
        Message msg = new Message("", "<msg><hover><tooltip><b>Title</b><br/><i>Text</i></tooltip><b>Mouse hover here</b></hover></msg>");
        assertEquals(BOLD+"Mouse hover here", transformer.toLegacy(msg));
    }
}
