package br.com.gamemods.minecity.api;

import org.assertj.core.internal.cglib.core.Local;
import org.junit.Test;
import org.mockito.internal.matchers.And;
import org.mockito.internal.matchers.Contains;
import org.mockito.internal.matchers.Or;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

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

    @Test
    public void testReplaceTokens() throws Exception
    {
        assertEquals("\\${number}", quoteReplacement("${number}"));
        assertEquals("ab\\${number}c", quoteReplacement("ab${number}c"));
        assertEquals("ab2c", replaceTokens("ab${number}c", new Object[]{"number",2}));
        assertEquals("ab${number}c", replaceTokens("ab${number}c"));
        assertEquals("a:1,b:2,c:3,d:${d}", replaceTokens("a:${a},b:${b},c:${c},d:${d}", new Object[][]{
                {"a",1},{"b",2},{"c",3}
        }));
        Locale pt = new Locale("pt"), en = Locale.ENGLISH;
        assertEquals("Price: 3.430.508$", replaceTokens(pt, "Price: ${val}$"   , new Object[]{"val",3430508}));
        assertEquals("Price: 3,430,508$", replaceTokens(en, "Price: ${val}${$}", new Object[]{"val",3430508}));
        Date date = new Date(1468870254120L);
        assertEquals("Date: 18/jul/2016", replaceTokens(pt,"Date: ${1}", new Object[]{1,date}));
        assertEquals("Date: Jul 18, 2016", replaceTokens(en,"Date: ${1}", new Object[]{1,date}));

        assertEquals("Date: "+DateFormat.getDateInstance(DateFormat.SHORT, pt).format(date),
                replaceTokens(pt,"Date: ${1}", new Object[]{1,date, DateFormat.SHORT}));
        assertEquals("Date: "+DateFormat.getDateInstance(DateFormat.SHORT, en).format(date),
                replaceTokens(en,"Date: ${1}", new Object[]{1,date, DateFormat.SHORT}));

        assertEquals("Date: "+DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, pt).format(date),
                replaceTokens(pt,"Date: ${1}", new Object[]{1,date, DateFormat.SHORT, DateFormat.SHORT}));
        assertEquals("Date: "+DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL, en).format(date),
                replaceTokens(en,"Date: ${1}", new Object[]{1,date, DateFormat.SHORT, DateFormat.FULL}));

        assertEquals("Value: 2.450,33", replaceTokens(pt, "Value: ${v}", new Object[]{"v",2450.33f}));
        assertEquals("Value: 2,450", replaceTokens(pt, "Value: ${v}", new Object[]{"v",2450.33f, NumberFormat.getIntegerInstance(en)}));

        assertEquals("My name is joserobjr and nil is null", replaceTokens("My name is ${name} and nil is ${nu}", new Object[][]{
                {"name","joserobjr"},{"nu",null}
        }));
    }
}