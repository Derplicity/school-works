import java.util.ArrayList;
import java.util.Scanner;

/**
 * The SumsToN class produces a list of all the different ways to get a collection of positive
 * integers adding up to n.
 * 
 * @author Michael Kerl
 */
public class SumsToN {

  private int n;
  private ArrayList<ArrayList<Integer>> sumsList;

  /**
   * Print a list of sums which add up to a user defined integer N.
   * @param args
   */
  public static void main(String[] args) {
    Scanner userInput = new Scanner(System.in);

    // Get value of N from user
    System.out.print("Value of N: ");
    int nValue = userInput.nextInt();
    userInput.close();

    // Create SumsToN instance with given N
    SumsToN sumsToN = new SumsToN(nValue);
    sumsToN.getSums();
    sumsToN.printSums();
  }

  /**
   * Constructor which initializes field members based on input.
   * 
   * @param n
   */
  private SumsToN(int n) {
    this.n = n;
    sumsList = new ArrayList<>();
  }

  /**
   * Call getSums() method with initial parameters.
   */
  private void getSums() {
    getSums(new ArrayList<>(), 0, 1);
  }

  /**
   * Recursively calculate the sums which add up to N.
   * 
   * @param prevList - previous list of numbers in the sum.
   * @param prevSum - sum of the numbers in the previous list.
   * @param prevI - previous value of I (automatically prunes duplicates such as 1+2 and 2+1).
   */
  private void getSums(ArrayList<Integer> prevList, int prevSum, int prevI) {
    for (int i = prevI; i <= n; i++) {
      ArrayList<Integer> newList = new ArrayList<>();
      int newSum = prevSum + i;

      newList.addAll(prevList);
      newList.add(i);
    
      if (newSum == n) {
        sumsList.add(newList);
      }
      
      if (newSum < n) {
        getSums(newList, newSum, i);
      }
    }
  }

  /**
   * Print the list of sums which add up to N.
   */
  private void printSums() {
    System.out.println("Possible Sums (" + sumsList.size() + "):");
    for (ArrayList<Integer> sumList : sumsList) {
      System.out.print("=> ");

      for (int i = 0; i < sumList.size(); i++) {
        if (i != 0) {
          System.out.print(" + ");
        }

        System.out.print(sumList.get(i));
      }

      System.out.println();
    }
  }

}