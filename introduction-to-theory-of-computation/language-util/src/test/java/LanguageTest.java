import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
/**
 * Tests for class {@link Language} using the JUnit4 framework.
 *
 * @author  Dr. Jody Paul
 * @version 1.1
 */
public class LanguageTest {
    /** Empty string. */
    static final String EMPTY_STRING = "";
    /** Language with no strings. */
    static final Language EMPTY_LANG = new Language();

    /**
     * Verify behavior of the isEmpty predicate.
     */
    @Test
    public void isEmptyTest() {
        assertTrue(EMPTY_LANG.isEmpty());
        Language lang = new Language();
        assertTrue(lang.isEmpty());
        lang.addString(EMPTY_STRING);
        assertFalse(lang.isEmpty());
        lang.addString("abba");
        assertFalse(lang.isEmpty());
    }

    /**
     * Verify cardinality report of a language.
     */
    @Test
    public void cardinalityTest() {
        assertEquals(0, EMPTY_LANG.cardinality());
        Language lang = new Language();
        assertEquals(0, lang.cardinality());
        lang.addString(EMPTY_STRING);
        assertEquals(1, lang.cardinality());
        lang.addString("abba");
        assertEquals(2, lang.cardinality());
        lang.addString("abba");
        assertEquals(2, lang.cardinality());
    }

    /**
     * Verify check on whether string is in the language.
     */
    @Test
    public void includesTest() {
        assertFalse(EMPTY_LANG.includes(EMPTY_STRING));
        assertFalse(EMPTY_LANG.includes("a"));
        assertFalse(EMPTY_LANG.includes("abba"));
        Language lang = new Language();
        lang.addString(EMPTY_STRING);
        assertTrue(lang.includes(EMPTY_STRING));
        assertFalse(lang.includes("a"));
        assertFalse(lang.includes("abba"));
        lang.addString("abba");
        assertTrue(lang.includes(EMPTY_STRING));
        assertFalse(lang.includes("a"));
        assertTrue(lang.includes("abba"));
        lang.addString("a");
        assertTrue(lang.includes(EMPTY_STRING));
        assertTrue(lang.includes("a"));
        assertTrue(lang.includes("abba"));
        assertFalse(lang.includes("b"));
        ArrayList<String> stringsToAdd = new ArrayList<>();
        stringsToAdd.add("z");
        stringsToAdd.add("za");
        stringsToAdd.add("zaa");
        lang.addAllStrings(stringsToAdd);
        assertTrue(lang.includes("a"));
        assertTrue(lang.includes("abba"));
        assertTrue(lang.includes("z"));
        assertTrue(lang.includes("za"));
        assertTrue(lang.includes("zaa"));
        assertFalse(lang.includes("c"));
    }

    /**
     * Verify behavior of iterator over empty language.
     */
    @Test
    public void iteratorEmptyLanguageTest() {
        assertNotNull(EMPTY_LANG.iterator());
        assertFalse(EMPTY_LANG.iterator().hasNext());
    }

    /**
     * Verify behavior of iterator over language with cardinality 1.
     */
    @Test
    public void iteratorSizeOneTest() {
        Language lang = new Language();
        lang.addString("abba");
        Iterator<String> lit = lang.iterator();
        assertTrue(lit.hasNext());
        assertEquals("abba", lit.next());
        assertFalse(lit.hasNext());
    }

    /**
     * Verify behavior of iterator over language with cardinality 2,
     * including items inserted in either order.
     */
    @Test
    public void iteratorSizeTwoTest() {
        Language lang = new Language();
        lang.addString("abba");
        Iterator<String> lit = lang.iterator();
        assertTrue(lit.hasNext());
        assertEquals("abba", lit.next());
        assertFalse(lit.hasNext());
        lang.addString("a");
        lit = lang.iterator();
        assertTrue(lit.hasNext());
        assertEquals("a", lit.next());
        assertTrue(lit.hasNext());
        assertEquals("abba", lit.next());
        assertFalse(lit.hasNext());
        lang = new Language();
        lang.addString("a");
        lit = lang.iterator();
        assertTrue(lit.hasNext());
        assertEquals("a", lit.next());
        assertFalse(lit.hasNext());
        lang.addString("abba");
        lit = lang.iterator();
        assertTrue(lit.hasNext());
        assertEquals("a", lit.next());
        assertTrue(lit.hasNext());
        assertEquals("abba", lit.next());
        assertFalse(lit.hasNext());
    }

    /**
     * Verify behavior of iterator over language arbitrary strings.
     */
    @Test
    public void iteratorTest() {
        Language lang = new Language();
        lang.addString("abba");
        lang.addString("a");
        lang.addString(EMPTY_STRING);
        lang.addString("b");
        Iterator<String> lit = lang.iterator();
        assertTrue(lit.hasNext());
        assertEquals(EMPTY_STRING, lit.next());
        assertTrue(lit.hasNext());
        assertEquals("a", lit.next());
        assertTrue(lit.hasNext());
        assertEquals("abba", lit.next());
        assertTrue(lit.hasNext());
        assertEquals("b", lit.next());
        assertFalse(lit.hasNext());
    }

    /**
     * Verify iterable interface using enhanced for.
     */
    @Test
    public void iteratableTest() {
        Language lang = new Language();
        lang.addString("abba");
        lang.addString("a");
        lang.addString(EMPTY_STRING);
        lang.addString("b");
        String allTogether = "";
        for (String s : lang) {
            allTogether += s + ",";
        }
        assertEquals(",a,abba,b,", allTogether);
    }


    /**
     * Verify concatenation of two empty languages.
     */
    @Test
    public void concatenateBothEmptyTest() {
        Language lang = new Language();
        Language concatLang = lang.concatenate(EMPTY_LANG);
        assertEquals(0, concatLang.cardinality());
    }

    /**
     * Verify concatenation with an empty language.
     */
    @Test
    public void concatenateWithEmptyTest() {
        Language lang = new Language();
        lang.addString("abcd");
        Language concatLang = lang.concatenate(EMPTY_LANG);
        assertEquals(0, concatLang.cardinality());
        concatLang = EMPTY_LANG.concatenate(lang);
        assertEquals(0, concatLang.cardinality());
    }

    /**
     * Verify concatenation two two non-empty languages.
     */
    @Test
    public void concatenateNonEmptyTest() {
        Language lang1 = new Language();
        lang1.addString("a");
        Language lang2 = new Language();
        lang2.addString("x");
        Language concatLang = lang1.concatenate(lang2);
        assertEquals(1, concatLang.cardinality());
        assertTrue(concatLang.includes("ax"));
        concatLang = lang2.concatenate(lang1);
        assertEquals(1, concatLang.cardinality());
        assertTrue(concatLang.includes("xa"));
        lang1.addString("b");
        lang1.addString("aba");
        // lang1 is {"a" "aba" "b"}
        concatLang = lang1.concatenate(lang2);
        assertEquals(3, concatLang.cardinality());
        assertTrue(concatLang.includes("ax"));
        assertTrue(concatLang.includes("bx"));
        assertTrue(concatLang.includes("abax"));
        lang2.addString("y");
        // lang2 is {"x" "y"}
        concatLang = lang1.concatenate(lang2);
        assertEquals(6, concatLang.cardinality());
        assertTrue(concatLang.includes("ax"));
        assertTrue(concatLang.includes("bx"));
        assertTrue(concatLang.includes("abax"));
        assertTrue(concatLang.includes("ay"));
        assertTrue(concatLang.includes("by"));
        assertTrue(concatLang.includes("abay"));
        lang1.addString(EMPTY_STRING);
        // lang1 is {"" "a" "aba" "b"}
        concatLang = lang1.concatenate(lang2);
        assertEquals(8, concatLang.cardinality());
        assertTrue(concatLang.includes("x"));
        assertTrue(concatLang.includes("y"));
    }

    /** Verify the overridden equality predicate. */
    @Test
    public void equalsTest() {
        assertTrue(EMPTY_LANG.equals(EMPTY_LANG));
        Language lang1 = new Language();
        assertTrue(EMPTY_LANG.equals(lang1));
        assertTrue(lang1.equals(EMPTY_LANG));
        assertEquals(EMPTY_LANG, lang1);
        Language lang2 = new Language();
        assertTrue(lang1.equals(lang2));
        assertEquals(lang1, lang2);
        lang2.addString("ab");
        assertFalse(lang1.equals(lang2));
        lang1.addString("ab");
        assertTrue(lang1.equals(lang2));
        assertTrue(lang2.equals(lang1));
        assertEquals(lang1, lang2);
    }
}
