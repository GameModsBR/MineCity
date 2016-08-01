package br.com.gamemods.minecity.api.command;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static br.com.gamemods.minecity.api.StringUtil.identity;
import static br.com.gamemods.minecity.api.StringUtil.replaceTokens;
import static br.com.gamemods.minecity.api.command.LegacyFormat.*;

public class MessageTransformer
{
    private Map<String, Element> messages = new HashMap<>();
    private Document doc;
    private DocumentBuilder documentBuilder;
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilder = factory.newDocumentBuilder();
        }
        catch(ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void parseXML(InputStream in) throws IOException, SAXException
    {
        Element root;
        doc = documentBuilder.parse(in);
        root = doc.getDocumentElement();
        if(!"minecity-messages".equals(root.getTagName()))
            throw new SAXException("This is not a minecity-messages XML! Root:"+root.getTagName());

        NodeList nodes = root.getElementsByTagName("msg");
        int len = nodes.getLength();
        for(int i = 0; i < len; i++)
        {
            Element msgTag = (Element) nodes.item(i);
            Element parent = msgTag;
            Stack<String> stack = new Stack<>();
            while(!root.equals(parent = (Element) parent.getParentNode()))
                stack.push(identity(parent.getTagName()));

            StringBuilder sb = new StringBuilder();
            String id;
            while(!stack.isEmpty())
                sb.append(stack.pop()).append(".");
            sb.append(msgTag.getAttribute("id"));
            id = sb.toString();

            setElement(id, msgTag);
        }
    }

    protected Element compile(String fallback)
    {
        if(fallback.startsWith("<msg>"))
            try
            {
                return documentBuilder.parse(new ByteArrayInputStream(fallback.getBytes())).getDocumentElement();
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
            catch(SAXException e)
            {
                throw new IllegalArgumentException(e);
            }

        Element msg = doc.createElement("msg");
        msg.setTextContent(fallback);
        return msg;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private String toLegacy(Element message, String baseFormat, Object[][] args)
    {
        StringBuilder sb = new StringBuilder();

        Deque<Node> queue = new ArrayDeque<>();
        Deque<String> formatQueue = new ArrayDeque<>();
        queue.add(message);
        if(baseFormat.startsWith(RESET.toString()))
            baseFormat = baseFormat.substring(2);

        EnumSet<LegacyFormat> formats = LegacyFormat.formatAt(baseFormat, baseFormat.length());
        Iterator<LegacyFormat> iterator = formats.iterator();
        iterator.remove();
        iterator.forEachRemaining(sb::append);
        String inherited = sb.toString();
        sb.setLength(0);

        formatQueue.add(baseFormat);
        String currentFormat = baseFormat;
        while(!queue.isEmpty())
        {
            Node current = queue.pop();
            String format = formatQueue.pop();

            short nodeType = current.getNodeType();
            if(nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE)
            {
                String text = current.getNodeValue();
                boolean lSpace = false, rSpace = false;
                if(nodeType == Node.TEXT_NODE)
                {
                    char l = text.charAt(0), r = text.charAt(text.length()-1);
                    text = text.trim();
                    lSpace = sb.length() > 0 && (text.length() == 0 || text.charAt(0) != l);
                    rSpace= !queue.isEmpty() && (text.length() <= 1 || text.charAt(text.length()-1) != r);
                }

                if(!currentFormat.equals(format))
                {
                    if(!currentFormat.isEmpty())
                        sb.append(RESET);
                    sb.append(format);
                    currentFormat = format;

                }

                if(lSpace)
                    sb.append(' ');

                sb.append(replaceTokens(text, args));

                if(rSpace)
                    sb.append(' ');
                continue;
            }
            else if(nodeType == Node.ELEMENT_NODE)
            {
                String name = current.getNodeName();
                switch(name)
                {
                    case "b": format = currentFormat+BOLD; break;
                    case "i": format = currentFormat+ITALIC; break;
                    case "u": format = currentFormat+UNDERLINE; break;
                    case "s": format = currentFormat+STRIKE; break;
                    case "o": format = currentFormat+MAGIC; break;
                    case "c":
                    {
                        String[] codes = ((Element) current).getAttribute("code").split(",");
                        StringBuilder formatBuilder = new StringBuilder();
                        for(String code: codes)
                        {
                            if(code.length() == 1)
                                formatBuilder.append(MARK).append(code);
                            else
                                try
                                {
                                    formatBuilder.append(LegacyFormat.valueOf(code.toUpperCase()));
                                }
                                catch(IllegalArgumentException ignored)
                                {}
                        }

                        format = currentFormat+formatBuilder;
                        break;
                    }
                    case "black": format = BLACK+inherited; break;
                    case "darkblue": format = DARK_BLUE+inherited; break;
                    case "darkgreen": format = DARK_GREEN+inherited; break;
                    case "darkaqua": format = DARK_AQUA+inherited; break;
                    case "darkred": format = DARK_RED+inherited; break;
                    case "darkpurple": format = DARK_PURPLE+inherited; break;
                    case "gold": format = GOLD+inherited; break;
                    case "gray": format = GRAY+inherited; break;
                    case "darkgray": format = DARK_GRAY+inherited; break;
                    case "blue": format = BLUE+inherited; break;
                    case "green": format = GREEN+inherited; break;
                    case "aqua": format = AQUA+inherited; break;
                    case "red": format = RED+inherited; break;
                    case "lightpurple": format = LIGHT_PURPLE+inherited; break;
                    case "yellow": format = YELLOW+inherited; break;
                    case "white": format = WHITE+inherited; break;
                    case "reset": format = RESET+baseFormat; break;
                }
            }

            NodeList childNodes = current.getChildNodes();
            int len = childNodes.getLength();
            for(int i = len-1; i >= 0; i--)
            {
                queue.push(childNodes.item(i));
                formatQueue.push(format);
            }
        }

        return sb.toString();
    }

    public String toLegacy(Message message)
    {
        return toLegacy(message, "");
    }

    public String toLegacy(Message message, String base)
    {
        return getElement(message.getId()).map(e-> toLegacy(e, base, message.getArgs()))
                .orElseGet(()-> toLegacy(message.getFallback(), base, message.getArgs()));
    }

    public String toLegacy(String message)
    {
        return toLegacy(message, "", new Object[0][]);
    }

    public String toLegacy(String message, String baseFormat, Object[][] args)
    {
        Map<String, Object[]> delayed = new HashMap<>(1);
        if(args != null)
            for(int i = 0; i < args.length; i++)
            {
                Object[] arg = args[i];
                if(arg != null && arg.length >= 2 && arg[1] instanceof Message)
                {
                    delayed.put(arg[0].toString(), arg);
                    args[i] = null;
                }
            }

        String result;
        if(message.startsWith("<msg>"))
            result = toLegacy(compile(message), baseFormat, args);
        else
            result = replaceTokens(message, args);

        EnumSet<LegacyFormat> inherited = LegacyFormat.formatAt(baseFormat, baseFormat.length());
        Iterator<LegacyFormat> iterator = inherited.iterator();
        LegacyFormat color = iterator.next();
        LegacyFormat inheritedColor = color;
        iterator.remove();
        StringBuilder inheritedFormat = new StringBuilder();
        iterator.forEachRemaining(inheritedFormat::append);

        if(inheritedColor == RESET && delayed.isEmpty() && inheritedFormat.length() == 0)
            return result;

        StringBuilder sb = new StringBuilder(), token = new StringBuilder();
        char[] chars = result.toCharArray();
        EnumSet<LegacyFormat> format = EnumSet.copyOf(inherited);
        boolean buildingToken = false;
        for(int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if(buildingToken)
            {
                if(c >= 'a' && c <= 'z' || c >= 'A' && c <='Z' || c == '.' || c == '_' || c == '-' || c >= '0' && c <= '9')
                {
                    token.append(c);
                    continue;
                }

                buildingToken = false;
                if(c == '}')
                {
                    String key = token.toString();
                    token.setLength(0);

                    Object[] arg = delayed.get(key);
                    Message msg = (Message) arg[1];
                    StringBuilder f = new StringBuilder(color.toString());
                    format.forEach(f::append);
                    String inline = toLegacy(msg, f.toString());
                    sb.append(inline);
                    if(inline.contains(Character.toString(MARK)))
                        sb.append(f);
                    continue;
                }
                else
                {
                    sb.append("${").append(token);
                    token.setLength(0);
                }
            }

            if(c == MARK && i + 1 < chars.length)
            {
                LegacyFormat code = LegacyFormat.forCode(chars[i+1]);
                if(code != null)
                {
                    i++;

                    if(code.format)
                    {
                        sb.append(code);
                        format.add(code);
                    }
                    else
                    {
                        format.clear();
                        format.addAll(inherited);

                        if(code == RESET)
                        {
                            color = inheritedColor;
                            sb.append(inheritedColor).append(inheritedFormat);
                        }
                        else
                        {
                            color = code;
                            sb.append(code);
                            sb.append(inheritedFormat);
                        }
                    }

                    continue;
                }
            }
            else if(c == '$' && i + 2 < chars.length && chars[i+1]=='{')
            {
                buildingToken = true;
                i++;
                continue;
            }

            sb.append(c);
        }

        if(buildingToken)
            sb.append("${").append(token);

        return sb.toString();
    }

    public String toSimpleText(Message message)
    {
        return LegacyFormat.clear(toLegacy(message));
    }

    public Optional<Element> getElement(String id)
    {
        return Optional.ofNullable(messages.get(id));
    }

    public void setElement(String id, Element tag)
    {
        messages.put(id, tag);
    }

    public Element removeElement(String id)
    {
        return messages.remove(id);
    }
}
