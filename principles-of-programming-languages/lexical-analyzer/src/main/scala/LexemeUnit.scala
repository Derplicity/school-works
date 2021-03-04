/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Homework 03 - LexemeUnit
 */

class LexemeUnit(private var lexeme: String, private var token: Token.Value) {

  def getLexeme() = lexeme

  def getToken() = token

  override def toString: String = "(" + lexeme + "," + token + ")"
}
