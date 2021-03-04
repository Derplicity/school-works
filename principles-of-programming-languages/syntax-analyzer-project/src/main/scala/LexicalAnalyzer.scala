import scala.io.Source

import LexicalAnalyzer.WORD_TO_TOKEN

/**
  * Project: Programming Assignment 1
  * Author: Michael Kerl
  * Version: 1.0
  */

/**
  * A Lexical Analyzer iterates through a given file, converting each symbol into its corresponding
  * lexeme unit.
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

class LexicalAnalyzer(private var source: String) extends Iterable[LexemeUnit] {

  private var input = ""
  for (line <- Source.fromFile(source).getLines)
    input += line + "\n"

  // determines the class of a given character
  private def getCharClass(c: Char): CharClass.Value = {
    if (LexicalAnalyzer.LETTERS.contains(c))
      CharClass.LETTER
    else if (LexicalAnalyzer.DIGITS.contains(c))
      CharClass.DIGIT
    else if (LexicalAnalyzer.BLANKS.contains(c))
      CharClass.BLANK
    else if (c == '+' || c == '-' || c == '*')
      CharClass.OPERATOR
    else if (c == '>' || c == '=' || c == '<')
      CharClass.COMPARATOR
    else if (c == ':')
      CharClass.DECLARATOR
    else if (c == ';')
      CharClass.PUNCTUATOR
    else
      CharClass.OTHER
  }

  // reads the input until a non-blank character is found, returning the input updated
  private def readBlanks: Unit = {
    var foundNonBlank = false
    while (input.length > 0 && !foundNonBlank) {
      val c = input(0)
      if (getCharClass(c) == CharClass.BLANK)
        input = input.substring(1)
      else
        foundNonBlank = true
    }
  }

  def iterator: Iterator[LexemeUnit] = {
    new Iterator[LexemeUnit] {

      override def hasNext: Boolean = {
        readBlanks
        input.length > 0
      }

      override def next(): LexemeUnit = {
        if (!hasNext)
          new LexemeUnit("", Token.EOF)
        else {
          var lexeme = ""
          readBlanks
          if (input.length == 0)
            new LexemeUnit(lexeme, Token.EOF)
          else {
            var c = input(0)
            var charClass = getCharClass(c)

            // Recognize words as either reserved words, identifiers or boolean literals
            if (charClass == CharClass.LETTER) {
              lexeme += c

              // Concatenate all letters contained in the input word
              var endOfWord = false
              while (input.length() > 0 && !endOfWord) {
                input = input.substring(1)

                c = input(0)
                charClass = getCharClass(c)

                if (
                  charClass == CharClass.LETTER || charClass == CharClass.DIGIT
                ) {
                  lexeme += c
                } else {
                  endOfWord = true
                }
              }

              // Create new reserved word lexeme unit if word is in reserved word map
              if (WORD_TO_TOKEN.contains(lexeme)) {
                return new LexemeUnit(lexeme, WORD_TO_TOKEN(lexeme))
              }

              // Create new boolean literal lexeme unit if word is either "true" or "false"
              if (lexeme == "true" || lexeme == "false") {
                return new LexemeUnit(lexeme, Token.BOOL_LITERAL)
              }

              // If not reserved word or boolean literal, create new identifier lexeme unit
              return new LexemeUnit(lexeme, Token.IDENTIFIER)
            }

            // Recognize integer literals
            if (charClass == CharClass.DIGIT) {
              lexeme += c

              // Concatenate all numbers contained in the input literal
              var endOfNumber = false
              while (input.length() > 0 && !endOfNumber) {
                input = input.substring(1)

                c = input(0)
                charClass = getCharClass(c)

                if (charClass == CharClass.DIGIT) {
                  lexeme += c
                } else {
                  endOfNumber = true
                }
              }

              return new LexemeUnit(lexeme, Token.INT_LITERAL)

            }

            // Recognize operators
            if (charClass == CharClass.OPERATOR) {
              input = input.substring(1)
              lexeme += c

              if (c == '+') {
                return new LexemeUnit(lexeme, Token.ADD_OP)
              }

              if (c == '-') {
                return new LexemeUnit(lexeme, Token.SUB_OP)
              }

              if (c == '*') {
                return new LexemeUnit(lexeme, Token.MUL_OP)
              }
            }

            // Recognize comparators
            if (charClass == CharClass.COMPARATOR) {
              input = input.substring(1)
              lexeme += c

              c = input(0)
              charClass = getCharClass(c)

              if (charClass == CharClass.COMPARATOR) {
                input = input.substring(1)
                lexeme += c
              }

              if (lexeme == ">") {
                return new LexemeUnit(lexeme, Token.GT_SIGN)
              }

              if (lexeme == ">=") {
                return new LexemeUnit(lexeme, Token.GTE_SIGN)
              }

              if (lexeme == "=") {
                return new LexemeUnit(lexeme, Token.EQUAL_SIGN)
              }

              if (lexeme == "<=") {
                return new LexemeUnit(lexeme, Token.LTE_SIGN)
              }

              if (lexeme == "<") {
                return new LexemeUnit(lexeme, Token.LT_SIGN)
              }
            }

            // Recognize declarators
            if (charClass == CharClass.DECLARATOR) {
              input = input.substring(1)
              lexeme += c

              c = input(0)

              if (c == '=') {
                input = input.substring(1)
                lexeme += c
              }

              if (lexeme == ":") {
                return new LexemeUnit(lexeme, Token.COLON)
              }

              if (lexeme == ":=") {
                return new LexemeUnit(
                  lexeme,
                  Token.COLON_EQUAL
                )
              }
            }

            // Recognize punctuators
            if (charClass == CharClass.PUNCTUATOR) {
              input = input.substring(1)
              lexeme += c

              if (c == ';') {
                return new LexemeUnit(lexeme, Token.SEMICOLON)
              }
            }

            // Recognize others
            if (charClass == CharClass.OTHER) {
              input = input.substring(1)
              lexeme += c

              if (c == '.') {
                return new LexemeUnit(lexeme, Token.DOT)
              }
            }

            // throw an exception if an unrecognizable symbol is found
            throw new Exception(
              "Lexical Analyzer Error: unrecognizable symbol '" + c + "' found!"
            )
          }
        }
      } // end next
    } // end 'new' iterator
  } // end iterator method
} // end LexicalAnalyzer class

object LexicalAnalyzer {
  val LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
  val DIGITS = "0123456789"
  val BLANKS = " \n\t"
  val WORD_TO_TOKEN = Map(
    "program" -> Token.PROGRAM,
    "var" -> Token.VAR,
    "Integer" -> Token.INTEGER,
    "Boolean" -> Token.BOOLEAN,
    "begin" -> Token.BEGIN,
    "end" -> Token.END,
    "read" -> Token.READ,
    "write" -> Token.WRITE,
    "if" -> Token.IF,
    "then" -> Token.THEN,
    "else" -> Token.ELSE,
    "while" -> Token.WHILE,
    "do" -> Token.DO
  )

  def main(args: Array[String]): Unit = {
    // check if source file was passed through the command-line
    if (args.length != 1) {
      print("Missing source file!")
      System.exit(1)
    }

    val lex = new LexicalAnalyzer(args(0))
    val it = lex.iterator
    while (it.hasNext) {
      val lexemeUnit = it.next()
      println(lexemeUnit)
    }
  } // end main method
} // end LexicalAnalyzer object
