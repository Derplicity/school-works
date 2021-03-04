import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a table-driven finite state automaton.
 * <p>
 * States are identified by the consecutive non-negative integers. 0 represents
 * the <em>start state</em>. A subset of the states are identified as <em>accept
 * states</em>. The alphabet is a set of symbols.
 * </p>
 *
 * <p>
 * A "state transition table" specifies the new state of the program based on
 * its current state and a symbol from the alphabet.
 * </p>
 * <em>Sample State Transition Table:</em>
 * 
 * <pre>
 *       a   b   c
 *     +---+---+---+
 *   0 | 1 | 2 | 3 |
 *     +---+---+---+
 *   1 | 0 | 2 | 3 |
 *     +---+---+---+
 *   2 | 4 | 2 | 3 |
 *     +---+---+---+
 *   3 | 4 | 2 | 3 |
 *     +---+---+---+
 *   4 | 4 | 4 | 4 |
 *     +---+---+---+
 * </pre>
 *
 * <ul>
 * <li>The program starts in state 0 and reads symbols from the input.</li>
 *
 * <li>As each symbol is read, the program finds the entry corresponding to its
 * current state and the new symbol, and transitions to the indicated
 * state.</li>
 *
 * <li>If the program ends in an accept state, it returns <code>true</code>;
 * otherwise it returns <code>false</code>.
 * </ul>
 *
 * <p>
 * For example, consider the program with five states {0, 1, 2, 3, 4}, an
 * alphabet with three symbols {a, b, c}, two identified accept states {2, 3},
 * and the state transition table given above.
 * </p>
 * <p>
 * Here are processing steps that would occur with input "abbc".
 * </p>
 *
 * <ul>
 * <li>The program starts in state 0, so uses the row at index 0 to find what to
 * do when reading the first symbol.</li>
 * <li>When "a" is read, the program locates the entry in the column
 * corresponding to "a" and transitions from state 0 to 1.</li>
 * <li>When the first "b" is read, the program refers to the row with index 1
 * and the column corresponding to "b", thus changes its state to 2.</li>
 * <li>When the second "b" is read, the program refers to the row with index 2
 * and the "b" column, and sets its state to 2 (unchanged).</li>
 * <li>When "c" is read, the program refers to the row with index 2 and the "c"
 * column, and sets its state to 3.</li>
 * <li>The end of the input has been reached.<br>
 * Since the program is in state 3, which is one of the accept states, the
 * program returns <code>true</code>.</li>
 * </ul>
 *
 * <p>
 * Tables are represented in a text file with the following attributes.
 * </p>
 * <ul>
 * <li>A header line indicates the symbols corresponding to the columns of the
 * table (comma delimited).</li>
 * <li>One line for each state. Comma-delimited column entries identify the new
 * state after reading the symbol associated with that column.</li>
 * <li>A footer line indicates the set of <em>accept</em> states, using set
 * notation, e.g., <code>{4,2,7}</code>.</li>
 * </ul>
 *
 * <p>
 * <em>Sample Text File Contents:</em>
 * </p>
 * 
 * <pre>
 * a,b,c
 * 1,2,3
 * 0,2,3
 * 4,2,3
 * 4,4,4
 * {2,3}
 * </pre>
 *
 * @author Dr. Jody Paul
 * @author Michael Kerl
 * @version 1.3.1
 */
public class TableDrivenFSA implements java.io.Serializable {
  /** Serialization version ID. */
  private static final long serialVersionUID = 202013L;

  /** Default delimiter. */
  private static final String DELIMITER = ",";

  /** Initial state. */
  private static final int INITIAL_STATE = 0;

  /** State transition table. */
  private int[][] stateTransitionTable;
  /** Alphabet symbols, ordered by column of state transition table. */
  private String[] alphabet;
  /** Identified accept states. */
  private int[] acceptStates;

  /**
   * Create an automaton using specifications from a text file.
   * 
   * @param tableFile the name of the file containing the comma-delimited
   *                  transition table. The first row gives the alphabet symbols
   *                  in order. Each subsequent row gives the new states
   *                  corresponding to the input symbols. The last row identifies
   *                  the accept states.
   */
  public TableDrivenFSA(final String tableFile) {
    List<String> rows = new ArrayList<>();
    // Read in data from file.
    try {
      rows = new ArrayList<>(Files.readAllLines(Paths.get(tableFile), StandardCharsets.UTF_8));
    } catch (IOException e) {
      System.err.println("Error during attempt to read table file. " + e);
    }
    // Process data assuming it is in correct format.
    int numStates = rows.size() - 2;
    if (numStates > 0) {
      this.alphabet = rows.get(0).split(DELIMITER);
      this.stateTransitionTable = new int[numStates][this.alphabet.length];
      for (int i = 1; i <= numStates; i++) {
        int column = 0;
        this.stateTransitionTable[i - 1] = new int[this.alphabet.length];
        for (String s : rows.get(i).split(DELIMITER)) {
          this.stateTransitionTable[i - 1][column++] = Integer.parseInt(s);
        }
      }
      String[] accepts = rows.get(rows.size() - 1).substring(1, rows.get(rows.size() - 1).length() - 1)
          .split(DELIMITER);
      this.acceptStates = Arrays.stream(accepts).mapToInt(x -> Integer.valueOf(x)).toArray();
    }
  }

  @Override
  public String toString() {
    String retVal = "";
    if (this.alphabet != null) {
      retVal = Stream.of(this.alphabet).collect(Collectors.joining(DELIMITER));
      retVal += "\n";
    }
    if (this.stateTransitionTable != null) {
      for (int[] row : this.stateTransitionTable) {
        retVal += Arrays.stream(row).mapToObj(String::valueOf).collect(Collectors.joining(DELIMITER));
        retVal += "\n";
      }
    }
    if (this.acceptStates != null) {
      retVal += "{";
      retVal += Arrays.stream(this.acceptStates).mapToObj(String::valueOf).collect(Collectors.joining(DELIMITER));
      retVal += "}\n";
    }
    return retVal;
  }

  /**
   * Determines the next state given a current state and an input symbol.
   * 
   * @param currentState the current state
   * @param inputSymbol  the input symbol
   * @return the next state according to the state transition table; if any
   *         parameter is invalid, returns the value of the state parameter
   */
  public int nextState(final int currentState, final String inputSymbol) {
    int stateRowIndex = currentState;
    int symbolColumnIndex = -1;

    // Get index of symbol in alphabet array; then said index will represent the
    // column index in the transition function.
    for (int i = 0; i < this.alphabet.length; i++) {
      if (this.alphabet[i].equals(inputSymbol)) {
        symbolColumnIndex = i;
        break;
      }
    }

    // Make sure that all variables are valid, otherwise return current state
    if (stateRowIndex < 0 || stateRowIndex >= this.stateTransitionTable.length || symbolColumnIndex < 0
        || symbolColumnIndex >= this.alphabet.length) {
      return currentState;
    }

    return this.stateTransitionTable[stateRowIndex][symbolColumnIndex];
  }

  /**
   * Process a given input string to determine FSA acceptance.
   * 
   * @param inputString the string to process (ignores null input)
   * @return true if the end state is an accept state, false otherwise
   */
  public boolean processString(final String inputString) {
    // Return false if invalid parameter
    if (inputString == null) {
      return false;
    }

    String[] inputSymbols = inputString.split("");
    int currentState = INITIAL_STATE;
    boolean isInAcceptState = false;

    // Iterate through the steps of the FSA
    for (String symbol : inputSymbols) {
      currentState = this.nextState(currentState, symbol);
    }

    // Check if the end state is an accept state
    for (int i = 0; i < this.acceptStates.length; i++) {
      if (this.acceptStates[i] == currentState) {
        isInAcceptState = true;
        break;
      }
    }

    return isInAcceptState;
  }
}
