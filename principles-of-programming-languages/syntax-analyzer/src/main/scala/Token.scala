/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Activity 07 - Token
 */

object Token extends Enumeration {
  val EOF        = Value
  val ADD_OP     = Value
  val SUB_OP     = Value
  val MUL_OP     = Value
  val DIV_OP     = Value
  val IDENTIFIER = Value
  val LITERAL    = Value
  val OPEN_PAR   = Value
  val CLOSE_PAR  = Value
}
