import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Scanner;

/**
 * A KeyGen creates and saves to files a pair of keys for both X and Y, as well as a symmetric key.
 * 
 * @author Michael Kerl
 * @version 1.0.0
 */
public class KeyGen {

  /**
   * Create and save to files a pair of keys for X, a pair of keys for Y, and a symmetric key
   * 
   * @param args from the command line
   */
  public static void main(String[] args) {
    generateAndSaveKeyPairToFiles("XPublic.key", "XPrivate.key");
    generateAndSaveKeyPairToFiles("YPublic.key", "YPrivate.key");
    generateAndSaveSymmetricKeyToFile("symmetric.key");
  }

  /**
   * Create and save to file a symmetric key which is created from 16 characters received via user
   * input.
   * 
   * @param fileName of the file in which to save the symmetric key
   */
  private static void generateAndSaveSymmetricKeyToFile(String fileName) {
    // Get symmetric key string from user input and make sure length is 16
    Scanner scanner = new Scanner(System.in);
    String symmetricKey = "";

    while (symmetricKey.length() != 16) {
      System.out.println("Please enter a 16 character symmetric key:");
      symmetricKey = scanner.nextLine();

      if (symmetricKey.length() != 16) {
        System.out.println("You entered " + symmetricKey.length() + " characters!");
      }
    }

    scanner.close();

    // Write symmetric key to file.
    writeToFile(fileName, new Object[] {symmetricKey});
  }

  /**
   * Create and save to file a key pair which is generated via a key pair generator.
   * 
   * @param publicFileName  of the file in which to save the public key
   * @param privateFileName of the file in which to save the private key
   */
  private static void generateAndSaveKeyPairToFiles(String publicFileName, String privateFileName) {
    try {
      // Create key generator
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(1024);

      // Generate key pair
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      PublicKey publicKey = keyPair.getPublic();
      PrivateKey privateKey = keyPair.getPrivate();

      // Create key factory
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      // Get key specs
      RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
      RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);

      // Save key specs to their respective files
      writeToFile(publicFileName,
          new Object[] {publicKeySpec.getModulus(), publicKeySpec.getPublicExponent()});
      writeToFile(privateFileName,
          new Object[] {privateKeySpec.getModulus(), privateKeySpec.getPrivateExponent()});

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }
  }

  /**
   * Write the given objects to a file with the given file name
   * 
   * @param fileName of the file in which to write the objects
   * @param objs     which should be written to the file
   */
  private static void writeToFile(String fileName, Object[] objs) {
    try {
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));

      // Write each object to file
      for (Object obj : objs) {
        oos.writeObject(obj);
      }

      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
