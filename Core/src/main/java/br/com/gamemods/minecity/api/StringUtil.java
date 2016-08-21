package br.com.gamemods.minecity.api;

import java.text.DateFormat;
import java.text.Format;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{
    /**
     * Converts a name to a case-insensitive with simple chars id
     * @param str Name
     * @return ID
     */
    public static String identity(String str)
    {
        return Normalizer.normalize(str.trim().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^a-z0-9]", "");
    }

    public static String replaceTokens(String text, Object[]... tokens)
    {
        return replaceTokens(Locale.getDefault(), text, tokens);
    }

    public static String quoteReplacement(String str)
    {
        return str.replaceAll("\\$","\\\\\\$");
    }

    public static String replaceTokens(Locale locale, String text, Object[]... tokens)
    {
        HashMap<String,String> replacements = new HashMap<>();
        if(tokens != null) for(Object[] token: tokens)
        {
            if(token == null || token.length < 2)
                continue;

            Object rep = token[1];
            Format format;
            if(token.length >= 3 && token[2] instanceof Format)
                format = (Format) token[2];
            else if(rep instanceof Integer || rep instanceof Long || rep instanceof Short || rep instanceof Byte)
                format = NumberFormat.getIntegerInstance(locale);
            else if(rep instanceof Float || rep instanceof Double)
                format = NumberFormat.getNumberInstance(locale);
            else if(rep instanceof Date)
            {
                if(token.length == 4)
                    format = DateFormat.getDateTimeInstance((int) token[2], (int) token[3], locale);
                else if(token.length == 3)
                    format = DateFormat.getDateInstance((int)token[2], locale);
                else
                    format = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
            }
            else format = null;

            replacements.put(token[0].toString(), format != null? format.format(rep) : String.valueOf(rep));
        }

        return replaceTokens(text, replacements);
    }

    public static String replaceTokens(String text, Map<String, String> replacements)
    {
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuffer builder = new StringBuffer();
        while (matcher.find())
        {
            String group = matcher.group(1);
            String replacement;
            if(group.equals("$"))
                replacement = "$";
            else
            {
                replacement = replacements.get(group);
                if(replacement == null)
                    replacement = "${"+group+"}";
            }

            matcher.appendReplacement(builder, quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    public static String escapeJson(String str)
    {
        return str.replace("\\", "\\\\").replaceAll("(\r\n|\r|\n)","\n").replace("\n","\\n").replace("\"", "\\\"");
    }
}
