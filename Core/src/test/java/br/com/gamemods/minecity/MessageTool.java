package br.com.gamemods.minecity;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTool
{
    public static void main(String[] args) throws Exception
    {
        JFrame frame = new JFrame("Message Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container panel = frame.getContentPane();

        JTextArea textArea = new JTextArea();
        try(InputStream is = MineCity.class.getResourceAsStream("/assets/minecity/messages.xml"))
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            byte[] buf = new byte[32];
            while((read = is.read(buf)) >= 0)
                out.write(buf, 0, read);
            textArea.setText(new String(out.toByteArray()));
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400,400));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());

        JButton buttonImport = new JButton("Import");
        buttonImport.addActionListener((e)-> textArea.setText(file(textArea.getText())));
        buttonPanel.add(buttonImport, BorderLayout.LINE_START);

        JButton buttonExport = new JButton("Export");
        buttonExport.addActionListener((e) ->
        {
            try
            {
                textArea.setText(export(textArea.getText()));
            }
            catch(Exception e1)
            {
                e1.printStackTrace();
            }
        });
        buttonPanel.add(buttonExport, BorderLayout.LINE_END);

        panel.add(buttonPanel, BorderLayout.PAGE_END);

        frame.pack();
        frame.setVisible(true);
    }

    public static String file(String file)
    {
        Globals globals = JsePlatform.standardGlobals();
        LuaTable table = new LuaTable();
        globals.set("L", table);
        LuaValue lua = globals.load(file);
        lua.call();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        factory.setValidating(false);
        Document doc;
        try
        {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilder = factory.newDocumentBuilder();
            doc = documentBuilder.newDocument();

            Element root = doc.createElement("minecity-messages");
            root.setAttribute("modified", "false");
            root.setAttribute("add-missing", "true");
            doc.appendChild(root);

            LuaValue[] keys = table.keys();
            for(LuaValue key1 : keys)
            {
                String key = key1.strvalue().toString();
                String val = table.get(key1).strvalue().toString();

                String[] groups = key.split("\\.");
                Element parent = root;
                String id = groups[groups.length - 1];
                for(int j = 0; j < groups.length - 1; j++)
                {
                    String group = groups[j];
                    NodeList childNodes = parent.getChildNodes();
                    Element groupNode = null;
                    for(int k = 0; k < childNodes.getLength(); k++)
                    {
                        Node node = childNodes.item(k);
                        if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(group))
                        {
                            groupNode = (Element) node;
                            break;
                        }
                    }

                    if(groupNode == null)
                    {
                        try
                        {
                            groupNode = doc.createElement(group);
                            parent.appendChild(groupNode);
                        }
                        catch(DOMException e)
                        {
                            if(e.code != DOMException.INVALID_CHARACTER_ERR)
                                throw e;

                            id = String.join(".", Arrays.asList(groups).subList(j, groups.length));
                            break;
                        }
                    }

                    parent = groupNode;
                }

                Element msg = doc.createElement("msg");
                msg.setAttribute("id", id);
                parent.appendChild(msg);

                /*Document sub = documentBuilder.parse(new InputSource(new StringReader("<root>" + val + "</root>")));
                Element otherRoot = (Element) doc.importNode(sub.getDocumentElement(), true);
                NodeList childNodes = otherRoot.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++)
                    msg.appendChild(childNodes.item(0));
                */
                msg.setTextContent("{"+key+"}");
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            String skeleton = new String(out.toByteArray());
            skeleton = skeleton.replaceAll("\r?\n", "\n");
            String[] split = skeleton.split("\n");
            for(int i = 0; i < keys.length; i++)
            {
                String search = "{" + keys[i] + "}";
                int indent = 0;
                for(String s : split)
                {
                    if(!s.contains(search))
                        continue;
                    Matcher matcher = Pattern.compile("^( +).*").matcher(s);
                    matcher.matches();
                    indent = matcher.group(1).length();
                    break;
                }
                String replace = table.get(keys[i]).toString();
                if(indent > 0)
                {
                    indent += 2;
                    String spaces = new String(new char[indent]).replace("\0", " ");
                    String[] rs = replace.split("\n");
                    if(rs.length > 1)
                    {
                        for(int j = 1; j < rs.length; j++)
                        {
                            rs[j] = spaces + rs[j];
                        }
                        replace = String.join("\n", rs);
                        replace += "\n"+spaces.substring(0, indent-2);
                    }
                }
                System.out.println(indent);
                skeleton = skeleton.replace(search, replace);
            }
            return skeleton;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String export(String from) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        Document doc = documentBuilder.parse(new InputSource(new StringReader(from)));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder("");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        Pattern indent = Pattern.compile(".+\n([ \t]+).*?(\n[ \t]+)?</msg>$", Pattern.DOTALL);
        NodeList nodes = doc.getElementsByTagName("msg");
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Element msg = (Element) nodes.item(i);
            sb.append("L[\"");
            int index = sb.length();
            Node parent = msg.getParentNode();
            while((parent = parent.getParentNode()) != null && parent != doc.getDocumentElement() && parent != doc)
                sb.insert(index, '.').insert(index, parent.getNodeName());
            index = sb.length()-1;
            sb.append(msg.getAttribute("id")).append("\"] = \"");

            NamedNodeMap attributes = msg.getAttributes();
            for(int j = 0; j < attributes.getLength(); j++)
                attributes.removeNamedItem(attributes.item(0).getNodeName());

            DOMSource source = new DOMSource(msg);
            StreamResult result = new StreamResult(bos);
            transformer.transform(source, result);
            String str = new String(bos.toByteArray()).replaceAll("\r?\n", "\n");
            bos.reset();
            Matcher matcher = indent.matcher(str);
            if(matcher.matches())
            {
                int remove = matcher.group(1).length();
                str = str.replaceAll("\n {"+remove+"}", "\n");
            }
            if(str.equals("<msg/>"))
                str = "";
            else
                str = str.substring(5, str.length()-6).trim();
            str = str.replace("\"", "\\\"");
            str = str.replaceAll("\\r?\\n", "\\\\n");
            sb.append(str).append("\"\n");
        }

        return sb.toString();
    }
}
