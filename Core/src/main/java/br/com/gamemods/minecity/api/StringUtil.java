package br.com.gamemods.minecity.api;

import java.text.Normalizer;

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

    private StringUtil(){}
}
