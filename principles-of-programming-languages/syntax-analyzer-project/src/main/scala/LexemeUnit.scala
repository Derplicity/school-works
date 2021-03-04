/**
  * Project: Programming Assignment 1
  * Author: Michael Kerl
  * Version: 1.0
  */

/**
  * A LexemeUnit pairs a string with a token identifier.
  */
class LexemeUnit(private var lexeme: String, private var token: Int) {

  def getLexeme() = lexeme

  def getToken() = token

  override def toString: String = "(" + lexeme + "," + token + ")"
}
