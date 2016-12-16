package br.com.gamemods.minecity.api.command;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.*;

import static br.com.gamemods.minecity.api.StringUtil.*;
import static br.com.gamemods.minecity.api.command.LegacyFormat.*;

public class MessageTransformer
{
    private final Map<String, Component> messages = new HashMap<>();
    private final DocumentBuilder documentBuilder;
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
        Document doc;
        synchronized(documentBuilder)
        {
            doc = documentBuilder.parse(in);
        }
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

            Component component = parse(msgTag);
            messages.put(id, component);
        }
    }

    protected Component toComponent(Message message)
    {
        try
        {
            String id = message.getId();
            if(id.isEmpty())
                return parse(message.getFallback());

            Component component = messages.get(id);
            if(component != null)
                return component.clone();

            component = parse(message.getFallback());
            messages.put(id, component);
            return component.clone();
        }
        catch(SAXException | CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String[] toMultilineLegacy(Message message)
    {
        Component component = toComponent(message);
        component.apply(Locale.getDefault(), message.getArgs());
        List<Component> split = new ArrayList<>(1);
        split.add(component);
        component.splitNewLines(split);
        return split.stream().map(Component::toString).toArray(String[]::new);
    }

    public String toLegacy(Message message)
    {
        Component component = toComponent(message);
        component.apply(Locale.getDefault(), message.getArgs());

        return component.toString();
    }

    public String toSimpleText(Message message)
    {
        return LegacyFormat.clear(toLegacy(message));
    }

    public String[] toMultilineSimpleText(Message message)
    {
        return toSimpleText(message).split("\n");
    }

    public String toJson(Message message)
    {
        Component component = toComponent(message);
        component.apply(Locale.getDefault(), message.getArgs());
        return toJson(component);
    }

    protected String toJson(Component component)
    {
        StringBuilder sb = new StringBuilder("[\"\",");
        component.toJson(sb);
        sb.append(']');
        return sb.toString();
    }

    protected Component parse(String message) throws SAXException
    {
        if(message.startsWith("<msg>"))
            try
            {
                Document doc;
                synchronized(documentBuilder)
                {
                    doc = documentBuilder.parse(new InputSource(new StringReader(message)));
                }
                return parse(doc.getDocumentElement());
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
            catch(SAXException e)
            {
                throw new SAXException("Message: "+message, e);
            }
        else
        {
            TextComponent textComponent = parseText(message);
            if(textComponent.color == null)
                textComponent.color = RESET;
            return textComponent;
        }
    }

    protected TextComponent parseText(String text)
    {
        TextComponent subStructure = new TextComponent("");
        TextComponent last = subStructure;
        StringBuilder sb = new StringBuilder(), token = new StringBuilder();
        boolean buildingToken = false;
        char[] chars = text.toCharArray();
        for(int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if(buildingToken)
            {
                if(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '.' || c == '_' || c == '-')
                {
                    token.append(c);
                    continue;
                }

                buildingToken = false;
                if(c == '}')
                {
                    String key = token.toString();
                    Deque<String> queue = last.tokens.computeIfAbsent(sb.length(), k-> new ArrayDeque<>(1));
                    queue.addFirst(key);
                    token.setLength(0);
                    continue;
                }
                else
                    sb.append("${").append(token);

                token.setLength(0);
            }

            LegacyFormat format;
            if(c == MARK && i+1 < chars.length && (format = forCode(chars[i+1])) != null)
            {
                i++;
                if(sb.length() > 0)
                {
                    last.text = sb.toString();
                    sb.setLength(0);

                    TextComponent next = new TextComponent("");
                    if(format.format)
                    {
                        next.color = last.color;
                        next.style.add(format);
                        next.style.addAll(last.style);
                    }
                    else if(format != RESET)
                    {
                        next.color = format;
                        next.style.addAll(last.style);
                    }

                    subStructure.extra.add(next);
                    next.parent = subStructure;
                    last = next;
                }
                else
                {
                    if(format.format)
                        last.style.add(format);
                    else if(format == RESET)
                        last.color = null;
                    else
                        last.color = format;
                }
            }
            else if(c == '$' && i+3 < chars.length && chars[i+1] == '{' && chars[i+2] != '}')
            {
                i++;
                buildingToken = true;
            }
            else
                sb.append(c);
        }

        last.text = sb.toString();

        return subStructure;
    }

    private Element[] getElements(Element element, int amount)
    {
        NodeList childNodes = element.getChildNodes();
        Element[] elements = new Element[amount];
        int len = childNodes.getLength();
        int next = 0;
        for(int i = 0; i < len && next < amount; i++)
        {
            Node childNode = childNodes.item(i);
            if(childNode.getNodeType() != Node.ELEMENT_NODE)
                continue;
            elements[next++] = (Element) childNode;
        }

        return elements;
    }

    protected TextComponent parse(Element root)
    {
        Deque<Struct> queue = new ArrayDeque<>();
        TextComponent rootComponent = new TextComponent("");
        rootComponent.color = RESET;
        NodeList rootNodes = root.getChildNodes();
        int l = rootNodes.getLength();
        for(int n = 0; n < l; n++)
            queue.add(new Struct(rootNodes.item(n), rootComponent));

        boolean firstText = true;
        Struct item;
        queue:
        while((item = queue.poll()) != null)
        {
            Node node = item.element;
            short nodeType = node.getNodeType();
            Component component = item.component;
            Hover hover = null;
            Click click = null;
            if(nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE)
            {
                String text = node.getTextContent();
                if(nodeType == Node.TEXT_NODE)
                {
                    text = text.replaceAll("\\s+", " ");

                    if(text.length() > 2)
                    {
                        char lc = text.charAt(0), rc = text.charAt(text.length()-1);
                        text = text.trim();

                        if(!firstText && (text.length() == 0 || text.charAt(0) != lc))
                            text = " "+text;

                        if(!queue.isEmpty() && (text.length() <= 1 || text.charAt(text.length()-1) != rc))
                            text += " ";
                    }

                    firstText = false;
                }

                TextComponent subStructure = parseText(text);
                if(component instanceof TextComponent)
                {
                    TextComponent current = (TextComponent)component;
                    if(current.color == null && current.text.isEmpty() && current.style.isEmpty() && current.extra.isEmpty())
                    {
                        current.text = subStructure.text;
                        current.tokens = subStructure.tokens;
                        current.color = subStructure.color;
                        current.style = subStructure.style;
                        current.extra = subStructure.extra;
                        current.extra.forEach(c-> c.parent=current);
                    }
                    else if(current.text.isEmpty() && current.extra.isEmpty())
                    {
                        current.text = subStructure.text;
                        current.tokens = subStructure.tokens;
                        if(subStructure.color != null)
                            current.color = subStructure.color;
                        current.style.addAll(subStructure.style);
                        current.extra = subStructure.extra;
                        current.extra.forEach(c-> c.parent=current);
                    }
                    else
                    {
                        component.extra.add(subStructure);
                        subStructure.parent = component;
                    }
                }
            }
            else if(nodeType == Node.ELEMENT_NODE)
            {
                Element element = (Element) node;
                boolean repeat;
                do
                {
                    repeat = false;
                    switch(element.getTagName())
                    {
                        case "hover":
                        {
                            Element[] elements = getElements(element, 2);
                            Element type = elements[0];
                            Element child = elements[1];
                            if(child == null)
                                continue queue;

                            switch(type.getTagName().toLowerCase())
                            {
                                case "tooltip":
                                    hover = new HoverMessage(parse(type));
                                    break;
                                case "entity":
                                    hover = new HoverEntity(type.getAttribute("id"), type.getAttribute("type"),
                                            parseText(type.getAttribute("name"))
                                    );
                                    break;
                                case "achievement":
                                    hover = new HoverAchievement(type.getAttribute("id"));
                                    break;
                                case "item":
                                    //TODO HoverItem
                                    break;
                                default:
                                    continue queue;
                            }

                            node = element = child;
                            repeat = true;
                        }
                        break;
                        case "click":
                        {
                            Element[] elements = getElements(element, 2);
                            Element type = elements[0];
                            Element child = elements[1];
                            if(child == null)
                                continue queue;

                            String value;
                            ClickAction action;
                            switch(type.getTagName().toLowerCase())
                            {
                                case "run":
                                    action = ClickAction.RUN;
                                    value = type.getAttribute("cmd");
                                    break;
                                case "suggest":
                                    action = ClickAction.SUGGEST;
                                    value = type.getAttribute("cmd");
                                    break;
                                case "url":
                                    action = ClickAction.OPEN_URL;
                                    value = type.getAttribute("url");
                                    break;
                                default:
                                    continue queue;
                            }

                            click = new ClickCommand(action, value);
                            node = element = child;
                            repeat = true;
                        }
                        break;
                    }
                } while(repeat);

                LegacyFormat format;
                switch(element.getTagName())
                {
                    case "black": format = BLACK; break;
                    case "darkblue": format = DARK_BLUE; break;
                    case "darkgreen": format = DARK_GREEN; break;
                    case "darkaqua": format = DARK_AQUA; break;
                    case "darkred": format = DARK_RED; break;
                    case "darkpurple": format = DARK_PURPLE; break;
                    case "gold": format = GOLD; break;
                    case "gray": format = GRAY; break;
                    case "darkgray": format = DARK_GRAY; break;
                    case "blue": format = BLUE; break;
                    case "green": format = GREEN; break;
                    case "aqua": format = AQUA; break;
                    case "red": format = RED; break;
                    case "lightpurple": format = LIGHT_PURPLE; break;
                    case "yellow": format = YELLOW; break;
                    case "white": format = WHITE; break;
                    case "reset":
                    case "tooltip":
                        format = RESET;
                        break;
                    case "o": format = MAGIC; break;
                    case "b": format = BOLD; break;
                    case "s": format = STRIKE; break;
                    case "u": format = UNDERLINE; break;
                    case "i": format = ITALIC; break;
                    case "br":
                        TextComponent text = new TextComponent("\n");
                        text.parent = component;
                        component.extra.add(text);
                        firstText = true;
                    default:
                        continue;
                }

                Component extra = new TextComponent("");
                extra.hover = hover;
                extra.click = click;
                extra.parent = component;
                component.extra.add(extra);
                extra.style.addAll(component.style);
                if(format.format)
                    extra.style.add(format);
                else
                {
                    extra.color = format;
                    if(format == RESET)
                        extra.color = null;
                }
                component = extra;
            }

            NodeList childNodes = node.getChildNodes();
            int len = childNodes.getLength();
            for(int i = len-1; i >= 0; i--)
                queue.push(new Struct(childNodes.item(i), component));
        }

        return rootComponent;
    }

    private class Struct
    {
        private Node element;
        private Component component;

        private Struct(Node element, Component component)
        {
            this.element = element;
            this.component = component;
        }

        @Override
        public String toString()
        {
            return element.toString();
        }
    }

    protected abstract class Component implements Cloneable
    {
        public LegacyFormat color = null;
        public EnumSet<LegacyFormat> style = EnumSet.noneOf(LegacyFormat.class);
        public Click click;
        public Hover hover;
        public Component parent;
        public List<Component> extra = new ArrayList<>(2);

        protected abstract void toJson(StringBuilder sb);

        public abstract boolean splitNewLines(List<Component> list);

        public void replaceBaseColor(LegacyFormat baseColor)
        {
            if(color == RESET)
                color = baseColor;

            extra.forEach(e-> e.replaceBaseColor(baseColor));
        }

        public void addFormat(EnumSet<LegacyFormat> format)
        {
            style.addAll(format);
        }

        public void apply(Locale locale, Object[][] args)
        {
            if(args == null)
                args = new Object[0][0];

            Map<String, Object> replacements = new HashMap<>(args.length);
            for(Object[] arg: args)
            {
                if(arg == null || arg.length < 2)
                    continue;

                String key = arg[0].toString();

                Object val = arg[1];
                if(val == null)
                    val = "null";

                if(arg.length > 2 && arg[2] instanceof Format)
                    val = ((Format)arg[2]).format(val);
                else if(val instanceof Integer || val instanceof Long || val instanceof Short || val instanceof Byte)
                    val = NumberFormat.getIntegerInstance(locale).format(val);
                else if(val instanceof Float || val instanceof Double)
                    val = NumberFormat.getNumberInstance(locale).format(val);
                else if(val instanceof Date)
                {
                    if(arg.length == 4)
                        val = DateFormat.getDateTimeInstance((int) arg[2], (int) arg[3], locale).format(val);
                    else if(arg.length == 3)
                        val = DateFormat.getDateInstance((int)arg[2], locale).format(val);
                    else
                        val = DateFormat.getDateInstance(DateFormat.DEFAULT, locale).format(val);
                }

                if(!(val instanceof String) && !(val instanceof Message))
                    val = val.toString();

                replacements.put(key, val);
            }

            replace(locale, replacements);
        }

        public void replace(Locale locale, Map<String, Object> args)
        {
            if(click != null)
                click.replace(locale, args);

            if(hover != null)
                hover.replace(locale, args);

            extra.forEach(c-> c.replace(locale, args));
        }

        @Override
        protected Component clone() throws CloneNotSupportedException
        {
            Component clone = (Component) super.clone();
            clone.color = color;
            clone.style = EnumSet.copyOf(style);
            if(click != null)
                clone.click = click.clone();
            if(hover != null)
                clone.hover = hover.clone();

            clone.extra = new ArrayList<>(extra.size());
            for(Component component : extra)
                clone.extra.add(component.clone());

            return clone;
        }

        public abstract String literalValue();

        public LegacyFormat displayColor()
        {
            LegacyFormat expectedColor = color;
            Component parent = this.parent;
            while(expectedColor == null && parent != null)
            {
                expectedColor = parent.color;
                parent = parent.parent;
            }

            if(expectedColor == null)
                expectedColor = RESET;

            return expectedColor;
        }

        public EnumSet<LegacyFormat> parentStyle()
        {
            EnumSet<LegacyFormat> parentStyle = EnumSet.noneOf(LegacyFormat.class);
            Component parent = this.parent;
            while(parent != null)
            {
                parentStyle.addAll(parent.style);
                parent = parent.parent;
            }

            return parentStyle;
        }

        public LegacyFormat parentColor()
        {
            LegacyFormat parentColor = null;
            Component parent = this.parent;
            while(parent != null)
            {
                parentColor = parent.color;
                parent = parent.parent;
            }

            if(parentColor == null)
                parentColor = RESET;

            return parentColor;
        }

        public EnumSet<LegacyFormat> displayFormat()
        {
            EnumSet<LegacyFormat> format = EnumSet.copyOf(this.style);
            format.addAll(parentStyle());
            return format;
        }

        public Hover effectiveHover()
        {
            Hover hover = this.hover;
            Component parent = this.parent;
            while(hover == null && parent != null)
            {
                hover = parent.hover;
                parent = parent.parent;
            }

            return hover;
        }

        public Click effectiveClick()
        {
            Click click = this.click;
            Component parent = this.parent;
            while(click == null && parent != null)
            {
                click = parent.click;
                parent = parent.parent;
            }

            return click;
        }

        @Override
        public String toString()
        {
            String value = literalValue();
            if(value.isEmpty() && extra.isEmpty())
                return value;

            LegacyFormat expectedColor = displayColor();
            EnumSet<LegacyFormat> parentStyle = parentStyle();
            LegacyFormat parentColor = parentColor();

            EnumSet<LegacyFormat> addedStyle = EnumSet.copyOf(this.style);
            addedStyle.removeAll(parentStyle);

            EnumSet<LegacyFormat> fullStyle = EnumSet.copyOf(parentStyle);
            fullStyle.addAll(addedStyle);

            StringBuilder sb = new StringBuilder();

            if(color != null && parentColor != color)
            {
                sb.append(color);
                fullStyle.forEach(sb::append);
            }
            else if(!addedStyle.isEmpty())
                addedStyle.forEach(sb::append);

            EnumSet<LegacyFormat> currentStyle = EnumSet.copyOf(fullStyle);

            sb.append(value);

            for(Component component : extra)
            {
                sb.append(component);

                if(component.color != null && component.color != expectedColor)
                {
                    sb.append(expectedColor);
                    fullStyle.forEach(sb::append);
                }

                if(!component.style.isEmpty())
                {
                    currentStyle.addAll(component.style);
                    if(!currentStyle.equals(fullStyle))
                    {
                        sb.append(expectedColor);
                        fullStyle.forEach(sb::append);

                        currentStyle.clear();
                        currentStyle.addAll(fullStyle);
                    }
                }
            }
            return sb.toString();
        }
    }

    protected final class TextComponent extends Component
    {
        public String text;
        public SortedMap<Integer, Deque<String>> tokens = new TreeMap<>(Comparator.reverseOrder());

        public TextComponent(String text)
        {
            this.text = text;
        }

        @Override
        public boolean splitNewLines(List<Component> list)
        {
            int lineBreak = text.indexOf('\n');
            if(lineBreak >= 0)
            {
                TextComponent other = new TextComponent(text.substring(lineBreak+1));
                text = text.substring(0, lineBreak);
                other.color = color;
                other.addFormat(style);
                other.hover = hover;
                other.click = click;
                other.extra.addAll(extra);
                extra.forEach(c-> c.parent = other);
                extra.clear();

                list.add(other);
                other.splitNewLines(list);
                return true;
            }

            List<Component> collect = new ArrayList<>(1);
            Iterator<Component> iterator = extra.iterator();
            while(iterator.hasNext())
            {
                Component component = iterator.next();
                if(component.splitNewLines(collect))
                {
                    for(Component line: collect)
                    {
                        Component newParent = new TextComponent("");
                        newParent.color = color;
                        newParent.addFormat(style);
                        newParent.hover = hover;
                        newParent.click = click;
                        newParent.extra.add(line);
                        line.parent = newParent;
                        list.add(newParent);
                    }

                    Component newParent = list.get(list.size()-1);
                    while(iterator.hasNext())
                    {
                        Component move = iterator.next();
                        iterator.remove();
                        move.parent = newParent;
                        newParent.extra.add(move);
                    }

                    newParent.splitNewLines(list);
                    return true;
                }
            }

            return false;
        }

        @Override
        protected TextComponent clone()
        {
            try
            {
                TextComponent clone = (TextComponent) super.clone();
                clone.tokens = new TreeMap<>(tokens.comparator());
                tokens.forEach((k,v)-> clone.tokens.put(k, new ArrayDeque<>(v)));
                return clone;
            }
            catch(CloneNotSupportedException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void replace(Locale locale, Map<String, Object> args)
        {
            super.replace(locale, args);

            if(tokens.isEmpty())
                return;

            LegacyFormat baseColor = displayColor();
            EnumSet<LegacyFormat> format = displayFormat();

            StringBuilder sb = new StringBuilder(text);
            for(Map.Entry<Integer, Deque<String>> entry : tokens.entrySet())
            {
                int index = entry.getKey();
                Deque<String> queue = entry.getValue();
                for(String t: queue)
                {
                    Object val = args.getOrDefault(t, "${" + t + "}");
                    if(val instanceof Message)
                    {
                        Message msg = (Message) val;
                        Component component = messages.get(msg.getId());
                        try
                        {
                            if(component != null)
                                component = component.clone();
                            else
                                component = parse(msg.getFallback());
                        }
                        catch(SAXException | CloneNotSupportedException e)
                        {
                            throw new RuntimeException(e);
                        }

                        component.replaceBaseColor(baseColor);
                        component.addFormat(format);
                        component.parent = this;

                        TextComponent split = new TextComponent(sb.substring(index));
                        split.parent = this;
                        extra.add(0, split);
                        extra.add(0, component);

                        sb.setLength(index);

                        component.apply(locale, msg.getArgs());
                        continue;
                    }
                    sb.insert(index, val);
                }
            }
            tokens.clear();
            String text = sb.toString();
            if(!text.contains(Character.toString(MARK)))
            {
                this.text = text;
                return;
            }

            TextComponent subStructure = parseText(text);
            this.text = "";
            extra.add(0, subStructure);
        }

        @Override
        public String literalValue()
        {
            if(tokens.isEmpty())
                return text;

            StringBuilder sb = new StringBuilder(text);
            tokens.forEach((i,q)-> q.forEach((t)-> sb.insert(i, "${"+t+"}")));
            return sb.toString();
        }

        @Override
        protected void toJson(StringBuilder sb)
        {
            sb.append("{\"text\":\"").append(escapeJson(literalValue())).append("\"");
            if(color != null && color != RESET)
                sb.append(",\"color\":\"").append(color.name().toLowerCase()).append('"');

            for(LegacyFormat format: style)
                switch(format)
                {
                    case BOLD: sb.append(",\"bold\":true"); break;
                    case ITALIC: sb.append(",\"italic\":true"); break;
                    case UNDERLINE: sb.append(",\"underlined\":true"); break;
                    case STRIKE: sb.append(",\"strikethrough\":true"); break;
                    case MAGIC: sb.append(",\"obfuscated\":true"); break;
                }

            if(click != null)
            {
                sb.append(",\"clickEvent\":");
                click.toJson(sb);
            }

            if(hover != null)
            {
                sb.append(",\"hoverEvent\":");
                hover.toJson(sb);
            }

            if(!extra.isEmpty())
            {
                sb.append(",\"extra\":[");
                for(Component comp: extra)
                {
                    comp.toJson(sb);
                    sb.append(',');
                }
                sb.setCharAt(sb.length()-1, ']');
            }

            sb.append('}');
        }
    }

    protected abstract class Click implements Cloneable
    {
        @Override
        protected Click clone() throws CloneNotSupportedException
        {
            return (Click) super.clone();
        }

        public final void replace(Locale locale, Map<String, Object> replacements)
        {
            Map<String, String> stringMap = new HashMap<>(replacements.size());
            replacements.forEach((k,v)-> stringMap.put(k, v instanceof Message? toSimpleText((Message)v) : String.valueOf(v)));
            replaceStrings(stringMap);
        }

        public abstract void replaceStrings(Map<String, String> replacements);

        protected abstract void toJson(StringBuilder sb);
    }

    protected final class ClickCommand extends Click
    {
        public String value;
        public ClickAction action;

        public ClickCommand(ClickAction action, String value)
        {
            this.action = action;
            this.value = value;
        }

        @Override
        public void replaceStrings(Map<String, String> replacements)
        {
            value = replaceTokens(value, replacements);
        }

        @Override
        protected ClickCommand clone()
        {
            try
            {
                return (ClickCommand) super.clone();
            }
            catch(CloneNotSupportedException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void toJson(StringBuilder sb)
        {
            sb.append("{\"action\":\"");
            switch(action)
            {
                case RUN: sb.append("run_command"); break;
                case SUGGEST: sb.append("suggest_command"); break;
                case OPEN_URL: sb.append("open_url"); break;
                default:
                    throw new UnsupportedOperationException("Unsupported click action: "+action);
            }
            sb.append("\",\"value\":\"").append(escapeJson(value)).append("\"}");
        }
    }

    protected enum ClickAction
    {
        RUN, SUGGEST, OPEN_URL
    }

    protected abstract class Hover implements Cloneable
    {
        @Override
        protected Hover clone() throws CloneNotSupportedException
        {
            return (Hover) super.clone();
        }

        public void replace(Locale locale, Map<String, Object> replacements)
        {
            Map<String, String> stringMap = new HashMap<>(replacements.size());
            replacements.forEach((k,v)-> stringMap.put(k, v instanceof Message? toSimpleText((Message)v) : String.valueOf(v)));
            replaceStrings(stringMap);
        }

        public void replaceStrings(Map<String, String> replacements)
        {}

        protected abstract void toJson(StringBuilder sb);
    }

    protected final class HoverMessage extends Hover
    {
        public TextComponent message;

        public HoverMessage(TextComponent message)
        {
            this.message = message;
        }

        @Override
        public void replace(Locale locale, Map<String, Object> replacements)
        {
            message.replace(locale, replacements);
        }

        @Override
        protected void toJson(StringBuilder sb)
        {
            sb.append("{\"action\":\"show_text\",\"value\":");
            message.toJson(sb);
            sb.append('}');
        }

        @Override
        protected Hover clone()
        {
            try
            {
                HoverMessage clone = (HoverMessage) super.clone();
                clone.message = message.clone();
                return clone;
            }
            catch(CloneNotSupportedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    protected final class HoverEntity extends Hover
    {
        public TextComponent name;
        public String type;
        public String id;

        public HoverEntity(String id, String type, TextComponent name)
        {
            this.id = id;
            this.type = type;
            this.name = name;
        }

        @Override
        public void replace(Locale locale, Map<String, Object> replacements)
        {
            super.replace(locale, replacements);
            name.replace(locale, replacements);
        }

        @Override
        public void replaceStrings(Map<String, String> replacements)
        {
            type = replaceTokens(type, replacements);
            id = replaceTokens(id, replacements);
        }

        @Override
        protected void toJson(StringBuilder sb)
        {
            sb.append("{\"action\":\"show_entity\",\"value\":\"{id:").append(escapeJson(id))
                    .append(",name:").append(escapeJson(name.toString())).append(",type:").append(escapeJson(type))
                    .append("}\"}");
        }

        @Override
        protected HoverEntity clone()
        {
            try
            {
                HoverEntity clone = (HoverEntity) super.clone();
                clone.name = name.clone();
                return clone;
            }
            catch(CloneNotSupportedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    protected final class HoverAchievement extends Hover
    {
        public String id;

        public HoverAchievement(String id)
        {
            this.id = id;
        }

        @Override
        public void replaceStrings(Map<String, String> replacements)
        {
            id = replaceTokens(id, replacements);
        }

        @Override
        protected HoverAchievement clone()
        {
            try
            {
                return (HoverAchievement) super.clone();
            }
            catch(CloneNotSupportedException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void toJson(StringBuilder sb)
        {
            sb.append("{\"action\":\"show_achievement\",\"value\":\"").append(escapeJson(id)).append("\"}");
        }
    }
}
