import LexicalAnalyzer.WORD_TO_TOKEN

import scala.io.Source

/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Homework 03 - Lexical Analyzer
 */

/*
stmt       = `declare` identifier { option }
identifier = `$` letter { letter }
letter     = `a` | `b` | … | `z` | `A` | `B` | … | `Z`
option     = mode | scale | precision | base
mode       = `real` | `complex`
scale      = `fixed` | `floating`
precision  = `single` | `double`
base       = `binary` | `decimal`
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
          new LexemeUnit("", Token.EOF)
        else {
          var lexeme = ""
          readBlanks
          if (input.length == 0)
            new LexemeUnit(lexeme, Token.EOF)
          else {
            var c = input(0)
            var charClass = getCharClass(c)

            // Recognize identifiers
            if (c == '$') {
              lexeme += c

              // Concatenate all letters contained in the input identifier
              var endOfIdentifier = false
              while (input.length() > 0 && !endOfIdentifier) {
                input = input.substring(1)

                c = input(0)
                charClass = getCharClass(c)

                if (charClass == CharClass.LETTER) {
                  lexeme += c
                } else {
                  endOfIdentifier = true
                }
              }

              // Make sure identifier contains at least one letter following '$'
              if (lexeme.length() > 1) {
                return new LexemeUnit(lexeme, Token.IDENTIFIER)
              }
            }

            // Recognize reserved words
            if (charClass == CharClass.LETTER) {
              lexeme += c

              // Concatenate all letters contained in the input word
              var endOfWord = false
              while (input.length() > 0 && !endOfWord) {
                input = input.substring(1)

                c = input(0)
                charClass = getCharClass(c)

                if (charClass == CharClass.LETTER) {
                  lexeme += c
                } else {
                  endOfWord = true
                }
              }

              // Make sure the word is in the reserved word map
              if (WORD_TO_TOKEN.contains(lexeme)) {
                return new LexemeUnit(lexeme, WORD_TO_TOKEN(lexeme))
              }
            }

            // throw an exception if an unrecognizable symbol is found
            throw new Exception(
              "Lexical Analyzer Error: unrecognizable symbol found!"
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
    "declare" -> Token.DECLARE,
    "real" -> Token.REAL,
    "complex" -> Token.COMPLEX,
    "fixed" -> Token.FIXED,
    "floating" -> Token.FLOATING,
    "single" -> Token.SINGLE,
    "double" -> Token.DOUBLE,
    "binary" -> Token.BINARY,
    "decimal" -> Token.DECIMAL
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
