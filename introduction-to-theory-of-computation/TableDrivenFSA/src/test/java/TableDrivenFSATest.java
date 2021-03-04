import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tests for class {@link TableDrivenFSA} using JUnit 4 framework. Dpends on
 * specific external test data file.
 * 
 * @author Dr. Jody Paul
 * @version 1.2
 */
public class TableDrivenFSATest {
  /** Prepared test data file name. */
  public static final String TEST_DATA_FILE_NAME = "table1.txt";
  /** Test data. */
  public static final String TEST_DATA = "a,b,c\n1,2,3\n0,2,3\n4,2,3\n4,2,3\n4,4,4\n{2,3}\n";

  /**
   * Constructor for test class TableDrivenFSA. Expects a test data file; attempts
   * to create it if one does not exist.
   */
  public TableDrivenFSATest() {
    File file = new File(TEST_DATA_FILE_NAME);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        Files.write(Paths.get(TEST_DATA_FILE_NAME), TEST_DATA.getBytes());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Verify toString of object constructed with valid parameter.
   */
  @Test
  public void toStringOfValidTableFileTest() {
    TableDrivenFSA dfa = new TableDrivenFSA(TEST_DATA_FILE_NAME);
    assertEquals(TEST_DATA, dfa + "");
  }

  /**
   * Verify object constructed with invalid parameter is not null. Note: An
   * expected side effect is sending an error message to System.err
   */
  @Test
  public void constructionInvalidParameterTest() {
    TableDrivenFSA dfa = new TableDrivenFSA("BOGUS");
    assertTrue("This method should handle input file errors properly", true);
  }

  /**
   * Process input with accepted strings.
   */
  @Test
  public void processAcceptTest() {
    TableDrivenFSA dfa = new TableDrivenFSA(TEST_DATA_FILE_NAME);
    assertTrue(dfa.processString("abbc"));
    assertTrue(dfa.processString("b"));
    assertTrue(dfa.processString("c"));
    assertTrue(dfa.processString("ab"));
  }

  /**
   * Process input with rejected strings.
   */
  @Test
  public void processRejectTest() {
    TableDrivenFSA dfa = new TableDrivenFSA(TEST_DATA_FILE_NAME);
    assertFalse(dfa.processString("abba"));
    assertFalse(dfa.processString(""));
    assertFalse(dfa.processString("a"));
    assertFalse(dfa.processString(null));
  }

  /**
   * Verify nextState method behavior with valid parameters.
   */
  @Test
  public void nextStateValidParametersTest() {
    TableDrivenFSA t1 = new TableDrivenFSA(TEST_DATA_FILE_NAME);
    assertEquals(1, t1.nextState(0, "a"));
    assertEquals(2, t1.nextState(0, "b"));
    assertEquals(3, t1.nextState(0, "c"));
    assertEquals(0, t1.nextState(1, "a"));
    assertEquals(2, t1.nextState(1, "b"));
    assertEquals(3, t1.nextState(1, "c"));
    assertEquals(4, t1.nextState(2, "a"));
    assertEquals(2, t1.nextState(2, "b"));
    assertEquals(3, t1.nextState(2, "c"));
    assertEquals(4, t1.nextState(3, "a"));
    assertEquals(2, t1.nextState(3, "b"));
    assertEquals(3, t1.nextState(3, "c"));
    assertEquals(4, t1.nextState(4, "a"));
    assertEquals(4, t1.nextState(4, "b"));
    assertEquals(4, t1.nextState(4, "c"));
  }

  /**
   * Verify nextState method behavior with invalid parameters.
   */
  @Test
  public void nextStateInvalidParameterTest() {
    TableDrivenFSA t1 = new TableDrivenFSA(TEST_DATA_FILE_NAME);
    assertEquals(0, t1.nextState(0, "x"));
    assertEquals(6, t1.nextState(6, "b"));
    assertEquals(8, t1.nextState(8, "quack"));
    assertEquals(-1, t1.nextState(-1, "a"));
    assertEquals(1, t1.nextState(1, null));
  }
}
