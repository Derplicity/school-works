import java.util.Scanner;

/**
 * The TEA_Encryption program implements the TEA decryption process.
 * <p>
 * <i>Example Usage:</i>
 * <ul>
 * INPUT:<br>
 * K[0] = A4E31F06<br>
 * K[1] = BB48C001<br>
 * K[2] = 10C4DF38<br>
 * K[3] = 200CA3B4<br>
 * L[2] = 280CC3D3<br>
 * R[2] = CE52A1EF
 * </ul>
 * <p>
 * <ul>
 * OUTPUT:<br>
 * L[2] = 280CC3D3<br>
 * L[1] = D35FB1A0<br>
 * L[0] = 28BF7D9A<br>
 * R[2] = CE52A1EF<br>
 * R[1] = 280CC3D3<br>
 * R[0] = D35FB1A0
 * </ul>
 * 
 * @author Michael Kerl
 * @version 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Tiny_Encryption_Algorithm">Tiny
 *      Encryption Algorithm</a>
 */
public class TEA_Decryption {

    /** The user input. */
    private static Scanner userInput;
    /** The delta values. */
    private static final int[] DELTA_VALUES = { 0x11111111, 0x22222222 };
    /** The key blocks. */
    private static long[] keyBlocks;
    /** The left text blocks. */
    private static long[] leftTextBlocks;
    /** The right text blocks. */
    private static long[] rightTextBlocks;

    /**
     * Decrypts ciphertext blocks received from the user with key blocks received
     * from the user and prints the resulting left and right text blocks, including
     * the final plaintext blocks.
     * 
     * @param args
     */
    public static void main(String[] args) {

        // Initializes new scanner for user input
        userInput = new Scanner(System.in);

        // Initializes arrays used to store keys and text blocks
        keyBlocks = new long[4];
        leftTextBlocks = new long[3];
        rightTextBlocks = new long[3];

        // Gets keys and ciphertext blocks from user
        getKeyBlocksFromUser();
        getLeftCiphertextFromUser();
        getRightCiphertextFromUser();

        // Closes user input scanner
        userInput.close();

        // Decrypts the text blocks over n rounds, where n is half the number of key
        // blocks
        for (int i = (keyBlocks.length / 2) - 1; i >= 0; i--) {

            decryptText(i);

        }

        // Prints both the left and right text blocks
        printLeftTextBlocks();
        printRightTextBlocks();

    }

    /**
     * Gets the key blocks from the user.
     */
    private static void getKeyBlocksFromUser() {

        for (int i = 0; i < keyBlocks.length; i++) {

            System.out.print("Please input K[" + i + "] in Hex String (without “0x”): ");

            // Converts user input hex string into a long, then stores in key block array
            keyBlocks[i] = Long.decode("0x" + userInput.next());

        }

    }

    /**
     * Gets the left ciphertext from the user.
     */
    private static void getLeftCiphertextFromUser() {

        int lastIndex = leftTextBlocks.length - 1;

        System.out.print("Please input L[" + lastIndex + "] in Hex String (without “0x”): ");

        // Converts user input hex string into a long, then stores in left text block
        // array
        leftTextBlocks[lastIndex] = Long.decode("0x" + userInput.next());

    }

    /**
     * Gets the right ciphertext from the user.
     */
    private static void getRightCiphertextFromUser() {

        int lastIndex = rightTextBlocks.length - 1;

        System.out.print("Please input R[" + lastIndex + "] in Hex String (without “0x”): ");

        // Converts user input hex string into a long, then stores in right text block
        // array
        rightTextBlocks[lastIndex] = Long.decode("0x" + userInput.next());

    }

    /**
     * Decrypts the text blocks using the Tiny Encryption Algorithm.
     * 
     * @param round The current round of decryption.
     */
    private static void decryptText(int round) {

        // The key block pair
        long keyBlock1 = keyBlocks[2 * round];
        long keyBlock2 = keyBlocks[(2 * round) + 1];

        // The delta value
        int delta = DELTA_VALUES[round];

        // The left and right input text blocks
        long inputLeftTextBlock = leftTextBlocks[round + 1];
        long inputRightTextBlock = rightTextBlocks[round + 1];

        // Sets the output right text block to the input left text block
        long outputRightTextBlock = inputLeftTextBlock;

        // Left shifts the input left text block by 4, then adds the result to the
        // first key block in the pair via addition-mod-2^32
        long a = additionMod2To32(inputLeftTextBlock << 4, keyBlock1);

        // Right shifts the input left text block by 5, then adds the result to the
        // second key block in the pair via addition-mod-2^32
        long b = additionMod2To32(inputLeftTextBlock >>> 5, keyBlock2);

        // Adds the input left text block to the delta value via addition-mod-2^32
        long c = additionMod2To32(inputLeftTextBlock, delta);

        // Sets the output left text block to the subtraction of the input right text
        // block and the combined a xor b xor c via subtraction-mod-2^32
        long outputLeftTextBlock = subtractionMod2To32(inputRightTextBlock, a ^ b ^ c);

        // Updates the left and right text block arrays with the output left and right
        // text blocks
        leftTextBlocks[round] = outputLeftTextBlock;
        rightTextBlocks[round] = outputRightTextBlock;

    }

    /**
     * Adds two numbers via addition-mod-2^32.
     * 
     * @param a
     * @param b
     * @return (a + b) % 2^32
     */
    private static long additionMod2To32(long a, long b) {
        return (a + b) & 0xffffffffL;
    }

    /**
     * Subtracts two numbers via addition-mod-2^32.
     * 
     * @param a
     * @param b
     * @return (a - b) % 2^32
     */
    private static long subtractionMod2To32(long a, long b) {
        return (a - b) & 0xffffffffL;
    }

    /**
     * Prints the left text blocks.
     */
    private static void printLeftTextBlocks() {

        for (int i = leftTextBlocks.length - 1; i >= 0; i--) {

            // Converts a left text block into a hex string with uppercase letters
            String hexString = Long.toHexString(leftTextBlocks[i]).toUpperCase();

            System.out.println("L[" + i + "] = " + hexString);

        }

    }

    /**
     * Prints the right text blocks.
     */
    private static void printRightTextBlocks() {

        for (int i = rightTextBlocks.length - 1; i >= 0; i--) {

            // Converts a right text block into a hex string with uppercase letters
            String hexString = Long.toHexString(rightTextBlocks[i]).toUpperCase();

            System.out.println("R[" + i + "] = " + hexString);

        }

    }

}