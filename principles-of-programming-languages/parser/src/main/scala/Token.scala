/**
  * Project: Programming Assignment 1
  * Author: Michael Kerl
  * Version: 1.0
  */

/**
  * The Token enumeration contains all of the lexeme token identifiers.
  */
object Token extends Enumeration {
  val EOF = 0
  val PROGRAM = 1
  val DOT = 2
  val IDENTIFIER = 3
  val VAR = 4
  val SEMICOLON = 5
  val COLON = 6
  val INTEGER = 7
  val BOOLEAN = 8
  val BEGIN = 9
  val END = 10
  val COLON_EQUAL = 11
  val READ = 12
  val WRITE = 13
  val IF = 14
  val THEN = 15
  val ELSE = 16
  val WHILE = 17
  val DO = 18
  val ADD_OP = 19
  val SUB_OP = 20
  val MUL_OP = 21
  val INT_LITERAL = 22
  val BOOL_LITERAL = 23
  val GT_SIGN = 24
  val GTE_SIGN = 25
  val EQUAL_SIGN = 26
  val LTE_SIGN = 27
  val LT_SIGN = 28
}
