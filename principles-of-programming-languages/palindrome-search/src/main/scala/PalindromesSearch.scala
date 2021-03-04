import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._
import java.io.PrintWriter
import java.io.File

/*
 * CS3210 - Principles of Programming Languages - Fall 2020
 * Instructor: Thyago Mota
 * Description: Prg02 - PalindromesSearch
 * Student(s) Name(s): Michael Kerl
 */

object PalindromesSearch {
  val OUTPUT_FILE_NAME = "output.txt"

  def main(args: Array[String]): Unit = {
    // Welcome message
    println("Welcome to the palindromic sequence project!")

    // Handle insufficient parameter error
    if (args.length < 2) {
      println(
        "Use: java PalindromesSearch n m [y]\n[y]: when informed, all palindromic sequences must be saved to a file"
      )
      return System.exit(1)
    }

    // Set parameters to variables
    val numberToAddUpTo = args(0).toInt
    val numberToContain = args(1).toInt
    val shouldSaveToFile =
      if (args.length == 3 && args(2).equals("y")) true else false

    // Display parameters
    println("Parameter n = " + numberToAddUpTo)
    println("Parameter m = " + numberToContain)

    // The highest number allowed in a valid palindrome which contains the number m
    // i.e. highest number refers to X, where m X m is a valid palindrome
    var highestNumber = numberToAddUpTo - (numberToContain * 2)

    // If highest number is less than the number to contain, it means that X in m X m is less
    // than m in i m i, so we must revert back to number to contain in order to not skip i m i.
    if (highestNumber < numberToContain) {
      highestNumber = numberToContain
    }

    var candidatePalindromes = new ArrayBuffer[ArrayBuffer[Byte]]()
    val finalPalindromes = new ArrayBuffer[ArrayBuffer[Byte]]()

    // Print starting message
    if (shouldSaveToFile) {
      println("Generating palindromic sequences...")
    }

    // Start time clock
    val startTime = System.nanoTime()

    // Create initial palindromes
    for (i <- 1 to highestNumber) {
      var initialPalindrome = new ArrayBuffer[Byte]()

      // Create initial odd palindromes
      if (i <= numberToAddUpTo) {
        initialPalindrome.addOne(i.toByte)
        candidatePalindromes.addOne(initialPalindrome)
      }

      initialPalindrome = new ArrayBuffer[Byte]()

      // Create initial even palindromes
      if (2 * i <= numberToAddUpTo) {
        initialPalindrome.prepend(i.toByte)
        initialPalindrome.append(i.toByte)
        candidatePalindromes.addOne(initialPalindrome)
      }
    }

    // Build out each possible palindrome until all candidates are either accepted or rejected
    while (candidatePalindromes.nonEmpty) {
      val nextPalindromes = new ArrayBuffer[ArrayBuffer[Byte]]()

      for (i <- candidatePalindromes.indices) {
        val parentPalindrome = candidatePalindromes(i)
        val parentSum = parentPalindrome.reduce((x, y) => (x + y).toByte).toInt

        if (
          parentSum == numberToAddUpTo
          && parentPalindrome.contains(numberToContain.toByte)
        ) {
          // Accept valid palindrome if also contains number m
          finalPalindromes.addOne(parentPalindrome)
        } else {
          breakable {
            for (j <- 1 to highestNumber) {
              val childPalindrome = parentPalindrome.clone()

              if (parentSum + (2 * j) <= numberToAddUpTo) {
                childPalindrome.prepend(j.toByte)
                childPalindrome.append(j.toByte)
                nextPalindromes.addOne(childPalindrome)
              } else {
                // else break here since all further sums will be greater than the current sum
                break
              }
            }
          }
        }
      }

      candidatePalindromes = nextPalindromes
    }

    // Write palindromes to file
    if (shouldSaveToFile) {
      val pw = new PrintWriter(new File(OUTPUT_FILE_NAME))

      for (i <- finalPalindromes.indices) {
        val palindrome = finalPalindromes(i)

        for (j <- palindrome.indices) {
          val number = palindrome(j)

          pw.write(number.toString)

          if (j < palindrome.length - 1) {
            pw.write(",")
          }
        }

        if (i < finalPalindromes.length - 1) {
          pw.write("\n")
        }
      }

      pw.close()
    }

    // End time clock
    val endTime = System.nanoTime()

    // Print finished message
    if (shouldSaveToFile) {
      println("Done!")
    } else {
      println(
        "Number of palindromic sequences found: " + finalPalindromes.length
      )
      println("It took me " + ((endTime - startTime) / 1000000000.00) + "s")
    }

  }
}
