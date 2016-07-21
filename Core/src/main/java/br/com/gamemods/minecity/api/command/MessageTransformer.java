package br.com.gamemods.minecity.api.command;

import br.com.gamemods.minecity.api.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

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
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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

    private String toLegacy(Element message, String baseFormat, Object[][] args)
    {
        StringBuilder sb = new StringBuilder();

        Deque<Node> queue = new ArrayDeque<>();
        Deque<String> formatQueue = new ArrayDeque<>();
        queue.add(message);
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
                    case "b": format = currentFormat+LegacyFormat.BOLD; break;
                    case "i": format = currentFormat+LegacyFormat.ITALIC; break;
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

    public String toLegacy(String message)
    {
        return toLegacy(message, "", new Object[0][]);
    }

    public String toLegacy(String message, String baseFormat, Object[][] args)
    {
        if(message.startsWith("<msg>"))
            return toLegacy(compile(message), baseFormat, args);

        return replaceTokens(message, args);
    }

    protected String toSimpleText(Element tag)
    {
        return tag.getTextContent().trim();
    }

    public String toSimpleText(Message message)
    {
        return replaceTokens(getSimpleText(message.getId()).orElseGet(message::getFallback), message.getArgs());
    }

    public Optional<String> getSimpleText(String id)
    {
        return getElement(id).map(this::toSimpleText);
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
