import scala.collection.mutable.ArrayBuffer

import SyntaxAnalyzer.{GRAMMAR_FILENAME, SLR_TABLE_FILENAME, TOKEN_TO_WORD}

/**
  * Project: Programming Assignment 1
  * Author: Michael Kerl
  * Version: 1.0
  */

/**
  * A Syntax Analyzer parses a file, using a slr table to convert sequences of symbols into
  * production variables which reduce completely only in the instance that the given sequence of
  * symbols is valid.
  *
  * Grammar:
  *   program = ´program´ identifier body ´.´
  *   identifier = letter { ( letter | digit ) }
  *   body = [ var_sct ] block
  *   var_sct = ´var´ var_dcl { ´;´ var_dcl }
  *   var_dcl = identifier { identifier } ´:´ type
  *   type = ´Integer´ | ´Boolean´
  *   block = ´begin´ stmt { ´;´ stmt } ´end´
  *   stmt = assgm_stmt | read_stmt | write_stmt | if_stmt | while_stmt | block
  *   assgm_stmt = identifier ´:=´ expr
  *   read_stmt = ´read´ identifier
  *   write_stmt = ´write´ ( identifier | literal )
  *   if_stmt = ´if´ bool_expr ´then´ stmt [ ´else´ stmt ]
  *   while_stmt = ´while´ bool_expr ´do´ stmt
  *   expr = arithm_expr | bool_expr
  *   arithm_expr = arithm_expr ( ´+´ | ´-´ ) term | term
  *   term = term ´*´ factor | factor
  *   factor = identifier | int_literal
  *   literal = int_literal | bool_literal
  *   int_literal = digit { digit }
  *   bool_litreal = ´true´ | ´false´
  *   bool_expr = bool_literal | arithm_expr ( ´>´ | ´>=´ | ´=´ | ´<=´ | ´<´ ) arithm_expr
  *   letter =  ´a´ | ´b´ | ´c´ | ´d´ | ´e´ | ´f´ | ´g´ | ´h´ | ´i´ | ´j´ | ´k´ | ´l´ | ´m´ | ´n´ |
  *             ´o´ | ´p´ | ´q´ | ´r´ | ´s´ | ´t´ | ´u´ | ´v´ | ´w´ | ´x´ | ´y´ | ´z´ | ´A´ | ´B´ |
  *             ´C´ | ´D´ | ´E´ | ´F´ | ´G´ | ´H´ | ´I´ | ´J´ | ´K´ | ´L´ | ´M´ | ´N´ | ´O´ | ´P´ |
  *             ´Q´ | ´R´ | ´S´ | ´T´ | ´U´ | ´V´ | ´W´ | ´X´ | ´Y´ | ´Z´
  *   digit =   ´0´ | ´1´ | ´2´ | ´3´ | ´4´ | ´5´ | ´6´ | ´7´ | ´8´ | ´9´
  *
  * @param source file
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

  // Gets the expected tokens message to be sent through an exception
  private def getExpectedTokensMessage(state: Int) = {
    var expectedTokens = new ArrayBuffer[String]
    TOKEN_TO_WORD.foreach((pair: (Int, String)) => {
      val key = pair._1;
      val value = pair._2;

      // Check if the token produces an action; in which case it is a valid "expected" token
      if (slrTable.getAction(state, key).length() > 0) {
        expectedTokens.addOne(value)
      }

    })

    var message = "Expected"

    for (i <- 0 until expectedTokens.length) {

      message += " " + expectedTokens(i)

      if (i == expectedTokens.length - 1) {
        message += "."
      } else if (i == expectedTokens.length - 2) {
        message += ", or"
      } else {
        message += ","
      }
    }

    message
  }

  def parse(): Tree = {

    // create a stack of trees
    val trees: ArrayBuffer[Tree] = new ArrayBuffer[Tree]

    // initialize the parser's stack of (state, symbol) pairs
    val stack: ArrayBuffer[String] = new ArrayBuffer[String]
    stack.append("0")

    // main parser loop
    while (true) {

      if (SyntaxAnalyzer.DEBUG)
        println("stack: " + stack.mkString(","))

      // update lexeme unit (if needed)
      getLexemeUnit()

      // get current state
      var state = stack.last.trim().toInt
      if (SyntaxAnalyzer.DEBUG)
        println("state: " + state)

      // get current token
      val token = lexemeUnit.getToken()

      // get action
      val action = slrTable.getAction(state, token)
      if (SyntaxAnalyzer.DEBUG)
        println("action: " + action)

      // if action is undefined, throw an exception
      if (action.length == 0)
        throw new Exception(
          "Syntax Analyzer Error! " + getExpectedTokensMessage(state)
        )

      // implement the "shift" operation if the action's prefix is "s"
      if (action(0) == 's') {

        // update the parser's stack
        stack.append(token + "")
        stack.append(action.substring(1))

        // create a new tree with the lexeme
        val tree = new Tree(lexemeUnit.getLexeme())

        // push the new tree onto the stack of trees
        trees.append(tree)

        // update lexemeUnit to null to acknowledge reading the input
        lexemeUnit = null
      }
      // implement the "reduce" operation if the action's prefix is "r"
      else if (action(0) == 'r') {

        // get the production to use
        val index = action.substring(1).toInt
        val lhs = grammar.getLHS(index)
        val rhs = grammar.getRHS(index)

        // update the parser's stack
        stack.dropRightInPlace(rhs.length * 2)
        state = stack.last.trim().toInt
        stack.append(lhs)
        stack.append(slrTable.getGoto(state, lhs))

        // create a new tree with the "lhs" variable as its label
        val newTree = new Tree(lhs)

        // add "rhs.length" trees from the right-side of "trees" as children of "newTree"
        for (tree <- trees.drop(trees.length - rhs.length))
          newTree.add(tree)

        // drop "rhs.length" trees from the right-side of "trees"
        trees.dropRightInPlace(rhs.length)

        // append "newTree" to the list of "trees"
        trees.append(newTree)
      }
      // implement the "accept" operation
      else if (action.equals("acc")) {

        // create a new tree with the "lhs" of the first production ("start symbol")
        val newTree = new Tree(grammar.getLHS(0))

        // add all trees as children of "newTree"
        for (tree <- trees)
          newTree.add(tree)

        // return "newTree"
        return newTree
      } else
        throw new Exception("Syntax Analyzer Error! Invalid action.")
    }
    throw new Exception("Syntax Analyzer Error! Unable to parse.")
  }
}

object SyntaxAnalyzer {

  val GRAMMAR_FILENAME = "grammar.txt"
  val SLR_TABLE_FILENAME = "slr_table.csv"

  val TOKEN_TO_WORD = Map(
    Token.PROGRAM -> "'program'",
    Token.DOT -> "'.'",
    Token.IDENTIFIER -> "'identifier'",
    Token.VAR -> "'var'",
    Token.SEMICOLON -> "';'",
    Token.COLON -> "':'",
    Token.INTEGER -> "'Integer'",
    Token.BOOLEAN -> "'Boolean'",
    Token.BEGIN -> "'begin'",
    Token.END -> "'end'",
    Token.COLON_EQUAL -> "':='",
    Token.READ -> "'read'",
    Token.WRITE -> "'write'",
    Token.IF -> "'if'",
    Token.THEN -> "'then'",
    Token.ELSE -> "'else'",
    Token.WHILE -> "'while'",
    Token.DO -> "'do'",
    Token.ADD_OP -> "'+'",
    Token.SUB_OP -> "'-'",
    Token.MUL_OP -> "'*'",
    Token.INT_LITERAL -> "'Integer literal'",
    Token.BOOL_LITERAL -> "'Boolean literal'",
    Token.GT_SIGN -> "'>'",
    Token.GTE_SIGN -> "'>='",
    Token.EQUAL_SIGN -> "'='",
    Token.LTE_SIGN -> "'<='",
    Token.LT_SIGN -> "'<'"
  )

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
