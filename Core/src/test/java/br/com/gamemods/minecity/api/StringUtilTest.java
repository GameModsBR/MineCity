package br.com.gamemods.minecity.api;

import org.junit.Test;

import static br.com.gamemods.minecity.api.StringUtil.*;
import static org.junit.Assert.*;

public class StringUtilTest
{

    @Test
    public void testIdentity() throws Exception
    {
        assertEquals("aaa", identity("aaa"));
        assertEquals("aaa", identity("a aa"));
        assertEquals("aaa", identity("a a a"));
        assertEquals("aaa", identity(" a a a"));
        assertEquals("aaa", identity(" a a a "));
        assertEquals("aaa", identity(" a.a a "));
        assertEquals("aaa", identity("'a a a$"));
        assertEquals("aaa1", identity("*a.a a1"));
        assertEquals("aaa1", identity("(a.a$a-1"));
        assertEquals("aaa1", identity("à.á ã+1"));
        assertEquals("", identity("䦎鏧稞 ナお つ穥襧きヘ 儥饣䩦ぎゅぢゃ"));
        assertEquals("aaa1", identity("@.a ª+æ aa ¹1"));
        assertEquals("a1a", identity("æḀȺ1ä"));
        assertEquals("aa", identity("ⱥÅ å"));
        assertEquals("nacao", identity("_NAÇÃO_"));
    }
}