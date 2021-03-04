/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Homework 03 - Token
 */

object Token extends Enumeration {
  val EOF = Value
  val DECLARE = Value
  val IDENTIFIER = Value
  val REAL = Value
  val COMPLEX = Value
  val FIXED = Value
  val FLOATING = Value
  val SINGLE = Value
  val DOUBLE = Value
  val BINARY = Value
  val DECIMAL = Value
}
