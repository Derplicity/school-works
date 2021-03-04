import SyntaxAnalyzer.{GRAMMAR_FILENAME, SLR_TABLE_FILENAME}
import Token.Value

import scala.collection.mutable.ArrayBuffer

/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Activity 09 - Syntax Analyzer
 */

/*
expression  = term expression'
expression' = ( ´+´  | ´-´ ) term expression' | epsilon
term        = factor term'
term'       = ( ´*´ | ´/´ ) factor term' | epsilon
factor      = identifier | literal | ´(´ expression ´)´
identifier  = letter { ( letter | digit ) }
letter      = ´a´ | ´b´ | ´c´ | ´d´ | ´e´ | ´f´ | ´g´ | ´h´ | ´i´ | ´j´ | ´k´ | ´l´ | ´m´
| ´n´ | ´o´ | ´p´ | ´q´ | ´r´ | ´s´ | ´t´ | ´u´ | ´v´ | ´w´ | ´x´ | ´y´ | ´z´
literal     = digit { digit }
digit       = ´0´ | ´1´ | ´2´ | ´3´ | ´4´ | ´5´ | ´6´ | ´7´ | ´8´ | ´9´
 */

class SyntaxAnalyzer(private var source: String) {

  private val it = new LexicalAnalyzer(source).iterator
  private var lexemeUnit: LexemeUnit = null
  private val grammar = new Grammar(GRAMMAR_FILENAME)
  private val slrTable = new SLRTable(SLR_TABLE_FILENAME)

  private def getLexemeUnit() = {
    if (lexemeUnit == null)
      lexemeUnit = it.next()
    if (SyntaxAnalyzer.DEBUG)
      println("lexemeUnit: " + lexemeUnit)
  }

  def parse(): Tree = {

    // TODO: create a stack of trees
    val trees: ArrayBuffer[Tree] = new ArrayBuffer[Tree]

    // TODO: initialize the parser's stack of (state, symbol) pairs
    val stack: ArrayBuffer[String] = new ArrayBuffer[String]
    stack.append("0")

    // TODO: main parser loop
    while (true) {

      if (SyntaxAnalyzer.DEBUG)
        println("stack: " + stack.mkString(","))

      // TODO: update lexeme unit (if needed)
      getLexemeUnit()

      // TODO: get current state
      var state = stack.last.trim().toInt
      if (SyntaxAnalyzer.DEBUG)
        println("state: " + state)

      // TODO: get current token
      val token = lexemeUnit.getToken()

      // TODO: get action
      val action = slrTable.getAction(state, token)
      if (SyntaxAnalyzer.DEBUG)
        println("action: " + action)

      // TODO: if action is undefined, throw an exception
      if (action.length == 0)
        throw new Exception("Syntax Analyzer Error!")

      // TODO: implement the "shift" operation if the action's prefix is "s"
      if (action(0) == 's') {

        // TODO: update the parser's stack
        stack.append(token + "")
        stack.append(action.substring(1))

        // TODO: create a new tree with the lexeme
        val tree = new Tree(lexemeUnit.getLexeme())

        // TODO: push the new tree onto the stack of trees
        trees.append(tree)

        // TODO: update lexemeUnit to null to acknowledge reading the input
        lexemeUnit = null
      }
      // TODO: implement the "reduce" operation if the action's prefix is "r"
      else if (action(0) == 'r') {

        // TODO: get the production to use
        val index = action.substring(1).toInt
        val lhs = grammar.getLHS(index)
        val rhs = grammar.getRHS(index)

        // TODO: update the parser's stack
        stack.dropRightInPlace(rhs.length * 2)
        state = stack.last.trim().toInt
        stack.append(lhs)
        stack.append(slrTable.getGoto(state, lhs))

        // TODO: create a new tree with the "lhs" variable as its label
        val newTree = new Tree(lhs)

        // TODO: add "rhs.length" trees from the right-side of "trees" as children of "newTree"
        for (tree <- trees.drop(trees.length - rhs.length))
          newTree.add(tree)

        // TODO: drop "rhs.length" trees from the right-side of "trees"
        trees.dropRightInPlace(rhs.length)

        // TODO: append "newTree" to the list of "trees"
        trees.append(newTree)
      }
      // TODO: implement the "accept" operation
      else if (action.equals("acc")) {

        // TODO: create a new tree with the "lhs" of the first production ("start symbol")
        val newTree = new Tree(grammar.getLHS(0))

        // TODO: add all trees as children of "newTree"
        for (tree <- trees)
          newTree.add(tree)

        // TODO: return "newTree"
        return newTree
      } else
        throw new Exception("Syntax Analyzer Error!")
    }
    throw new Exception("Syntax Analyzer Error!")
  }
}

object SyntaxAnalyzer {

  val GRAMMAR_FILENAME = "grammar.txt"
  val SLR_TABLE_FILENAME = "slr_table.csv"

  val TOKEN_EOF = 0
  val TOKEN_ADD_OP = 1
  val TOKEN_SUB_OP = 2
  val TOKEN_MUL_OP = 3
  val TOKEN_DIV_OP = 4
  val TOKEN_IDENTIFIER = 5
  val TOKEN_LITERAL = 6
  val TOKEN_OPEN_PAR = 7
  val TOKEN_CLOSE_PAR = 8

  val DEBUG = false

  def main(args: Array[String]): Unit = {
    // check if source file was passed through the command-line
    if (args.length != 1) {
      print("Missing source file!")
      System.exit(1)
    }

    val syntaxAnalyzer = new SyntaxAnalyzer(args(0))
    val parseTree = syntaxAnalyzer.parse()
    print(parseTree)
  }
}
