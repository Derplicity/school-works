/**
  * Project: Programming Assignment 1
  * Author: Michael Kerl
  * Version: 1.0
  */

/**
  * The CharClass enumeration contains all of the subcategories for characters.
  */
object CharClass extends Enumeration {
  val EOF = Value
  val LETTER = Value
  val DIGIT = Value
  val OPERATOR = Value
  val COMPARATOR = Value
  val DECLARATOR = Value
  val PUNCTUATOR = Value
  val BLANK = Value
  val OTHER = Value
}
