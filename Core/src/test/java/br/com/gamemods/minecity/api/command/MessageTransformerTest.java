package br.com.gamemods.minecity.api.command;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static br.com.gamemods.minecity.api.command.LegacyFormat.*;
import static org.junit.Assert.*;

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
    public void testLineBreak() throws Exception
    {
        assertEquals("Contains\nline \nbreaks", transformer.toLegacy(new Message("","<msg>Contains<br/> line <br/>\n\tbreaks\n</msg>")));
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

        component = transformer.parse("${a}${b}${c}");
        assertEquals("${a}${b}${c}", component.toString());
        component.apply(Locale.US, new Object[][]{{"a","1"},{"b",4},{"c",6}});
        assertEquals("146", component.toString());
    }

    @Test
    public void testLegacy() throws Exception
    {
        Message message = new Message("test.bold", "");
        assertEquals("This message has a bold word", transformer.toSimpleText(message));

        String legacy = transformer.toLegacy(new Message("","<msg>A <b>bold</b> word</msg>"));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("A ")));
        assertEquals(legacy, EnumSet.of(RESET, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("bold")));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf(" word")));

        legacy = transformer.toLegacy(new Message("","<msg>Has <b>bold and <i>italic</i></b> words</msg>"));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("Has ")));
        assertEquals(legacy, EnumSet.of(RESET, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("bold and ")));
        assertEquals(legacy, EnumSet.of(RESET, BOLD, ITALIC), LegacyFormat.formatAt(legacy, legacy.indexOf("italic")));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("words")));

        legacy = transformer.toLegacy(new Message("","<msg>Has <b>bold and <i>italic</i> words</b></msg>"));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("Has ")));
        assertEquals(legacy, EnumSet.of(RESET, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("bold and ")));
        assertEquals(legacy, EnumSet.of(RESET, BOLD, ITALIC), LegacyFormat.formatAt(legacy, legacy.indexOf("italic")));
        assertEquals(legacy, EnumSet.of(RESET, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf(" words")));

        assertEquals(BOLD+"Bold"+RESET+" spaces", transformer.toLegacy(new Message("","<msg><b>Bold</b>     spaces</msg>")));
        assertEquals(BOLD+"Bold"+RESET+"     spaces", transformer.toLegacy(new Message("","<msg><b>Bold</b><![CDATA[     spaces]]></msg>")));
        assertEquals("A "+BOLD+"Bold"+RESET+" word", transformer.toLegacy(new Message("","<msg>\n\tA\n\t<b>Bold</b>\n\tword\n</msg>")));
    }

    @Test
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
        String legacy = transformer.toLegacy(container);
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf("This contains an ")));
        assertEquals(legacy, EnumSet.of(RED), LegacyFormat.formatAt(legacy, legacy.indexOf("inline")));
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf(" message")));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        container = new Message("", GREEN+"This contains an "+BOLD+"${msg} message", new Object[]{"msg", inline});
        legacy = transformer.toLegacy(container);
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf("This contains an ")));
        assertEquals(legacy, EnumSet.of(RED, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("inline")));
        assertEquals(legacy, EnumSet.of(GREEN, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf(" message")));
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
        legacy = transformer.toLegacy(container);
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf("This contains an ")));
        assertEquals(legacy, EnumSet.of(RED), LegacyFormat.formatAt(legacy, legacy.indexOf("inline")));
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf(" message")));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        container = new Message("", "<msg><green>This contains an <b>${msg} message</b></green></msg>", new Object[]{"msg", inline});
        legacy = transformer.toLegacy(container);
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf("This contains an ")));
        assertEquals(legacy, EnumSet.of(RED, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("inline")));
        assertEquals(legacy, EnumSet.of(GREEN, BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf(" message")));
        assertEquals("This contains an inline message", transformer.toSimpleText(container));

        inline = new Message("", "<msg><red>inline <i>${param} a</i></red> param</msg>", new Object[]{"param","message with"});
        container = new Message("", "<msg>This <green>contains an <b>${inline} in</b> it</green>.</msg>", new Object[]{"inline", inline});
        legacy = transformer.toLegacy(container);
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf("This ")));
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf("contains an ")));
        assertEquals(legacy, EnumSet.of(RED,BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf("inline ")));
        assertEquals(legacy, EnumSet.of(RED,BOLD,ITALIC), LegacyFormat.formatAt(legacy, legacy.indexOf("message with")));
        assertEquals(legacy, EnumSet.of(RED,BOLD,ITALIC), LegacyFormat.formatAt(legacy, legacy.lastIndexOf(" a")));
        assertEquals(legacy, EnumSet.of(GREEN,BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf(" param")));
        assertEquals(legacy, EnumSet.of(GREEN,BOLD), LegacyFormat.formatAt(legacy, legacy.indexOf(" in")));
        assertEquals(legacy, EnumSet.of(GREEN), LegacyFormat.formatAt(legacy, legacy.indexOf(" it")));
        assertEquals(legacy, EnumSet.of(RESET), LegacyFormat.formatAt(legacy, legacy.indexOf(".")));
        assertEquals("This contains an inline message with a param in it.", transformer.toSimpleText(container));

        MessageTransformer.Component component = transformer.toComponent(container);
        List<MessageTransformer.Component> split = new ArrayList<>();
        split.add(component);
        boolean changed = component.splitNewLines(split);
        assertFalse(split.toString(), changed);
        assertEquals(split.toString(), 1, split.size());
    }

    @Test
    public void testSplit() throws Exception
    {
        Message message = new Message("","This has a \nline break");
        MessageTransformer.Component component = transformer.toComponent(message);
        List<MessageTransformer.Component> split = new ArrayList<>();
        split.add(component);
        boolean changed = component.splitNewLines(split);
        assertTrue(changed);
        assertEquals(2, split.size());

        message = new Message("","<msg>This <red>is <b><![CDATA[a\ncomplex\n]]><green><![CDATA[message\nwith]]></green>" +
                "<![CDATA[\nline breaks]]></b><![CDATA[\neverywhere]]></red></msg>");
        String[] legacy = transformer.toMultilineLegacy(message);
        assertEquals(6, legacy.length);
        assertEquals(legacy[0], EnumSet.of(RESET), LegacyFormat.formatAt(legacy[0], legacy[0].indexOf("This ")));
        assertEquals(legacy[0], EnumSet.of(RED), LegacyFormat.formatAt(legacy[0], legacy[0].lastIndexOf("is ")));
        assertEquals(legacy[0], EnumSet.of(RED, BOLD), LegacyFormat.formatAt(legacy[0], legacy[0].indexOf("a")));
        assertEquals(legacy[1], EnumSet.of(RED, BOLD), LegacyFormat.formatAt(legacy[1], legacy[1].indexOf("complex")));
        assertEquals(legacy[2], EnumSet.of(GREEN, BOLD), LegacyFormat.formatAt(legacy[2], legacy[2].indexOf("message")));
        assertEquals(legacy[3], EnumSet.of(GREEN, BOLD), LegacyFormat.formatAt(legacy[3], legacy[3].indexOf("with")));
        assertEquals(legacy[4], EnumSet.of(RED, BOLD), LegacyFormat.formatAt(legacy[4], legacy[4].indexOf("line breaks")));
        assertEquals(legacy[5], EnumSet.of(RED), LegacyFormat.formatAt(legacy[5], legacy[5].indexOf("everywhere")));

        String[] simple = transformer.toMultilineSimpleText(message);
        assertEquals("This is a", simple[0]);
        assertEquals("complex", simple[1]);
        assertEquals("message", simple[2]);
        assertEquals("with", simple[3]);
        assertEquals("line breaks", simple[4]);
        assertEquals("everywhere", simple[5]);
    }

    @Test @Ignore
    public void testSkip() throws Exception
    {
        Message msg = new Message("", "<msg><hover><tooltip><b>Title</b><br/><i>Text</i></tooltip><b>Mouse hover here</b></hover></msg>");
        assertEquals(BOLD+"Mouse hover here", transformer.toLegacy(msg));
    }
}
