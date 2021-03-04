import java.util.Scanner;

/**
 * The TEA_Encryption program implements the TEA encryption process.
 * <p>
 * <i>Example Usage:</i>
 * <ul>
 * INPUT:<br>
 * K[0] = A4E31F06<br>
 * K[1] = BB48C001<br>
 * K[2] = 10C4DF38<br>
 * K[3] = 200CA3B4<br>
 * L[0] = 28BF7D9A<br>
 * R[0] = D35FB1A0
 * </ul>
 * <p>
 * <ul>
 * OUTPUT:<br>
 * L[0] = 28BF7D9A<br>
 * L[1] = D35FB1A0<br>
 * L[2] = 280CC3D3<br>
 * R[0] = D35FB1A0<br>
 * R[1] = 280CC3D3<br>
 * R[2] = CE52A1EF
 * </ul>
 * 
 * @author Michael Kerl
 * @version 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Tiny_Encryption_Algorithm">Tiny
 *      Encryption Algorithm</a>
 */
public class TEA_Encryption {

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
     * Encrypts plaintext blocks received from the user with key blocks received
     * from the user and prints the resulting left and right text blocks, including
     * the final cryptotext blocks.
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

        // Gets keys and plaintext blocks from user
        getKeyBlocksFromUser();
        getLeftPlaintextFromUser();
        getRightPlaintextFromUser();

        // Closes user input scanner
        userInput.close();

        // Encrypts the text blocks over n rounds, where n is half the number of key
        // blocks
        for (int i = 0; i < keyBlocks.length / 2; i++) {

            encryptText(i);

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
     * Gets the left plaintext from the user.
     */
    private static void getLeftPlaintextFromUser() {

        System.out.print("Please input L[0] in Hex String (without “0x”): ");

        // Converts user input hex string into a long, then stores in left text block
        // array
        leftTextBlocks[0] = Long.decode("0x" + userInput.next());

    }

    /**
     * Gets the right plaintext from the user.
     */
    private static void getRightPlaintextFromUser() {

        System.out.print("Please input R[0] in Hex String (without “0x”): ");

        // Converts user input hex string into a long, then stores in right text block
        // array
        rightTextBlocks[0] = Long.decode("0x" + userInput.next());

    }

    /**
     * Encrypts the text blocks using the Tiny Encryption Algorithm.
     * 
     * @param round The current round of encryption.
     */
    private static void encryptText(int round) {

        // The key block pair
        long keyBlock1 = keyBlocks[2 * round];
        long keyBlock2 = keyBlocks[(2 * round) + 1];

        // The delta value
        int delta = DELTA_VALUES[round];

        // The left and right input text blocks
        long inputLeftTextBlock = leftTextBlocks[round];
        long inputRightTextBlock = rightTextBlocks[round];

        // Sets the output left text block to the input right text block
        long outputLeftTextBlock = inputRightTextBlock;

        // Left shifts the input right text block by 4, then adds the result to the
        // first key block in the pair via addition-mod-2^32
        long a = additionMod2To32(inputRightTextBlock << 4, keyBlock1);

        // Right shifts the input right text block by 5, then adds the result to the
        // second key block in the pair via addition-mod-2^32
        long b = additionMod2To32(inputRightTextBlock >>> 5, keyBlock2);

        // Adds the input right text block to the delta value via addition-mod-2^32
        long c = additionMod2To32(inputRightTextBlock, delta);

        // Sets the output right text block to the addition of the input left text block
        // and the combined a xor b xor c via addition-mod-2^32
        long outputRightTextBlock = additionMod2To32(inputLeftTextBlock, a ^ b ^ c);

        // Updates the left and right text block arrays with the output left and right
        // text blocks
        leftTextBlocks[round + 1] = outputLeftTextBlock;
        rightTextBlocks[round + 1] = outputRightTextBlock;

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
     * Prints the left text blocks.
     */
    private static void printLeftTextBlocks() {

        for (int i = 0; i < leftTextBlocks.length; i++) {

            // Converts a left text block into a hex string with uppercase letters
            String hexString = Long.toHexString(leftTextBlocks[i]).toUpperCase();

            System.out.println("L[" + i + "] = " + hexString);

        }

    }

    /**
     * Prints the right text blocks.
     */
    private static void printRightTextBlocks() {

        for (int i = 0; i < rightTextBlocks.length; i++) {

            // Converts a right text block into a hex string with uppercase letters
            String hexString = Long.toHexString(rightTextBlocks[i]).toUpperCase();

            System.out.println("R[" + i + "] = " + hexString);

        }

    }

}