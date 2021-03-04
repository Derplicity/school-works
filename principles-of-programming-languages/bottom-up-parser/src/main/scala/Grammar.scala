import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Project: Programming Assignment 1
  * Author: Michael Kerl
  * Version: 1.0
  */

/**
  * A Grammar consists of an array of productions, which are given via an input file of productions.
  * A Grammar's productions are interactable via methods.
  *
  * @param source file
  */
class Grammar(private var source: String) {

  private var productions = new ArrayBuffer[String]

  for (line <- Source.fromFile(source).getLines)
    productions += line

  def getLHS(index: Integer): String = {
    val production = productions(index)
    production.split("->")(0).trim()
  }

  def getRHS(index: Integer) = {
    val production = productions(index)
    production.split("->")(1).trim().split(" ")
  }

  override def toString: String = {
    var out = ""
    for (i <- 0 until productions.length)
      out += i + ". " + getLHS(i) + " -> " + getRHS(i).mkString(" ") + "\n"
    out
  }
}
