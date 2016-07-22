package br.com.gamemods.minecity.api.command;

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
    MAGIC('k'),
    BOLD('l'),
    STRIKE('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r')
    ;
    public static final char MARK = '\u00A7';
    public static final LegacyFormat[] CITY_COLORS = {
            DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_PURPLE, GOLD,
            GRAY, BLUE, GREEN, AQUA, LIGHT_PURPLE, YELLOW
    };
    public final char code;

    LegacyFormat(char c)
    {
        code = c;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString()
    {
        return new StringBuilder(2).append(MARK).append(code).toString();
    }
}
