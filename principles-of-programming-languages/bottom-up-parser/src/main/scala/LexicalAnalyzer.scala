import scala.io.Source

/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Activity 09 - Lexical Analyzer
 */

/*
expression = expression ( ´+´  | ´-´ ) term | term
term       = term ( ´*´ | ´/´ ) factor | factor
factor     = identifier | literal | ´(´ expression ´)´
identifier = letter { ( letter | digit ) }
letter     = ´a´ | ´b´ | ´c´ | ´d´ | ´e´ | ´f´ | ´g´ | ´h´ | ´i´ | ´j´ | ´k´ | ´l´ | ´m´ | ´n´ | ´o´ | ´p´ | ´q´ | ´r´ | ´s´ | ´t´ | ´u´ | ´v´ | ´w´ | ´x´ | ´y´ | ´z´
literal    = digit { digit }
digit      = ´0´ | ´1´ | ´2´ | ´3´ | ´4´ | ´5´ | ´6´ | ´7´ | ´8´ | ´9´
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
    else if (c == '+' || c == '-' || c == '*' || c == '/')
      CharClass.OPERATOR
    else if (c == '(' || c == ')')
      CharClass.DELIMITER
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
          new LexemeUnit("", SyntaxAnalyzer.TOKEN_EOF)
        else {
          var lexeme = ""
          readBlanks
          if (input.length == 0)
            new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_EOF)
          else {
            var c = input(0)
            var charClass = getCharClass(c)

            // TODO: recognize a letter followed by letters (or digits) as an identifier
            if (charClass == CharClass.LETTER) {
              input = input.substring(1)
              lexeme += c
              var noMoreLetterDigits = false
              while (!noMoreLetterDigits) {
                if (input.length == 0)
                  noMoreLetterDigits = true
                else {
                  c = input(0)
                  charClass = getCharClass(c)
                  if (charClass == CharClass.LETTER || charClass == CharClass.DIGIT) {
                    input = input.substring(1)
                    lexeme += c
                  }
                  else
                    noMoreLetterDigits = true
                }
              }
              return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_IDENTIFIER)
            }

            // TODO: recognize multiple digits as a literal
            if (charClass == CharClass.DIGIT) {
              input = input.substring(1)
              lexeme += c
              var noMoreDigits = false
              while (!noMoreDigits) {
                if (input.length == 0)
                  noMoreDigits = true
                else {
                  c = input(0)
                  charClass = getCharClass(c)
                  if (charClass == CharClass.DIGIT) {
                    input = input.substring(1)
                    lexeme += c
                  }
                  else
                    noMoreDigits = true
                }
              }
              return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_LITERAL)
            }

            // TODO: recognize operators
            if (charClass == CharClass.OPERATOR) {
              input = input.substring(1)
              lexeme += c
              c match {
                case '+' => return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_ADD_OP)
                case '-' => return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_SUB_OP)
                case '*' => return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_MUL_OP)
                case '/' => return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_DIV_OP)
              }
            }

            // TODO: recognize delimiters
            if (charClass == CharClass.DELIMITER) {
              input = input.substring(1)
              lexeme += c
              c match {
                case '(' => return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_OPEN_PAR)
                case ')' => return new LexemeUnit(lexeme, SyntaxAnalyzer.TOKEN_CLOSE_PAR)
              }
            }

            // throw an exception if an unrecognizable symbol is found
            throw new Exception("Lexical Analyzer Error: unrecognizable symbol found!")
          }
        }
      } // end next
    } // end 'new' iterator
  } // end iterator method
} // end LexicalAnalyzer class

object LexicalAnalyzer {
  val LETTERS = "abcdefghijklmnopqrstuvwxyz"
  val DIGITS  = "0123456789"
  val BLANKS  = " \n\t"

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