package br.com.gamemods.minecity.api.command;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public enum  LegacyFormat
{
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    RESET('r'),
    MAGIC('k', true),
    BOLD('l', true),
    STRIKE('m', true),
    UNDERLINE('n', true),
    ITALIC('o', true)
    ;
    public static final char MARK = '\u00A7';
    public static final LegacyFormat[] CITY_COLORS = {
            DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_PURPLE, GOLD,
            GRAY, BLUE, GREEN, AQUA, LIGHT_PURPLE, YELLOW, WHITE
    };
    public final char code;
    public final boolean format;
    public Object server;

    LegacyFormat(char c)
    {
        code = c;
        format = false;
    }

    LegacyFormat(char code, boolean format)
    {
        this.code = code;
        this.format = format;
    }

    @SuppressWarnings("unchecked")
    public <T> T server() throws ClassCastException
    {
        return (T) server;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString()
    {
        return new StringBuilder(2).append(MARK).append(code).toString();
    }

    public static String clear(String str)
    {
        return str.replaceAll(MARK+"[0-9a-fA-Fk-rK-R]", "");
    }

    public static EnumSet<LegacyFormat> formatAt(String str, int pos)
    {
        if(pos < 0)
            throw new IndexOutOfBoundsException("pos = "+pos);
        if(pos > str.length())
            throw new IndexOutOfBoundsException(pos+" > "+str.length());

        LegacyFormat color = RESET;
        EnumSet<LegacyFormat> format = EnumSet.noneOf(LegacyFormat.class);

        char[] chars = str.toCharArray();
        for(int i = 0; i < pos; i++)
        {
            char c = chars[i];
            if(c == MARK && i+1 < chars.length)
            {
                LegacyFormat code = forCode(chars[i+1]);
                if(code != null)
                {
                    i++;
                    if(code.format)
                        format.add(code);
                    else
                    {
                        color = code;
                        format.clear();
                    }
                }
            }
        }

        format.add(color);
        return format;
    }

    @Nullable
    public static LegacyFormat forCode(char c)
    {
        switch(c)
        {
            case '0': return BLACK;
            case '1': return DARK_BLUE;
            case '2': return DARK_GREEN;
            case '3': return DARK_AQUA;
            case '4': return DARK_RED;
            case '5': return DARK_PURPLE;
            case '6': return GOLD;
            case '7': return GRAY;
            case '8': return DARK_GRAY;
            case '9': return BLUE;
            case 'a':case 'A': return GREEN;
            case 'b':case 'B': return AQUA;
            case 'c':case 'C': return RED;
            case 'd':case 'D': return LIGHT_PURPLE;
            case 'e':case 'E': return YELLOW;
            case 'f':case 'F': return WHITE;
            case 'k':case 'K': return MAGIC;
            case 'l':case 'L': return BOLD;
            case 'm':case 'M': return STRIKE;
            case 'n':case 'N': return UNDERLINE;
            case 'o':case 'O': return ITALIC;
            case 'r':case 'R': return RESET;
            default: return null;
        }
    }
}
