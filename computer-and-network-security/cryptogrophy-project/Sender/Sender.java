import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A Sender encrypts a given message from the user via SHA256, RSA and AES.
 * 
 * @author Michael Kerl
 * @version 1.0.0
 */
public class Sender {

  /**
   * Encrypts the message retrieved from the user via SHA256, RSA and AES.
   * 
   * @param args from the command line
   */
  public static void main(String[] args) {
    try {
      // Get keys needed to encrypt message
      PrivateKey privateKey = getPrivateKeyFromFile("XPrivate.key");
      String symmetricKey = getSymmetricKeyFromFile("symmetric.key");

      // Get message file to encrypt
      Scanner scanner = new Scanner(System.in);
      System.out.print("Input the name of the message file: ");
      String fileName = scanner.nextLine();

      // Get the digital digest and message from user file
      Map<String, List<byte[]>> digitalDigestAndMessage =
          getDigitalDigestAndMessageFromFile(fileName);
      byte[] digitalDigest = digitalDigestAndMessage.get("digitalDigest").get(0);
      List<byte[]> message = digitalDigestAndMessage.get("message");

      // Ask user whether to invert 1st byte in digital digest for testing purposes
      System.out.println("Do you want to invert the 1st byte in SHA256(M)? (Y or N)");
      String yOrN = scanner.next();
      scanner.close();

      // Invert 1st byte if user responds in the affirmative
      if (yOrN.toLowerCase().equals("y")) {
        digitalDigest[0] = (byte) ~digitalDigest[0];
      }

      // Write digital digest to file and print such digital digest as hex string
      List<byte[]> bytesToWrite = new ArrayList<>();
      bytesToWrite.add(digitalDigest);
      writeToFile("message.dd", bytesToWrite);
      bytesToWrite.clear();
      printHexString(digitalDigest);

      // Encrypt digital digest via RSA Encryption using private key
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.ENCRYPT_MODE, privateKey);
      byte[] rsaEncryptedDigitalDigest = cipher.doFinal(digitalDigest);

      // Write RSA encrypted digital digest concatenated with the original message to file and print
      // encrypted digital digest as hex string
      bytesToWrite.add(rsaEncryptedDigitalDigest);
      for (int i = 0; i < message.size(); i++) {
        bytesToWrite.add(message.get(i));
      }
      writeToFile("message.ds-msg", bytesToWrite);
      bytesToWrite.clear();
      printHexString(rsaEncryptedDigitalDigest);

      // Encrypt digital signature and message combination via AES Encryption using symmetric key
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream("message.ds-msg"));
      List<byte[]> aesEncryptedMessage = new ArrayList<>();
      cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(symmetricKey.getBytes("UTF-8"), "AES"),
          new IvParameterSpec("AAAAAAAAAAAAAAAA".getBytes("UTF-8")));
      int bytesRead;
      while (true) {
        byte[] messageBytes = new byte[16];
        bytesRead = bis.read(messageBytes, 0, 16);
        if (bytesRead == -1) {
          break;
        }
        if (bis.available() == 0) {
          byte[] temp = new byte[bytesRead];
          for (int i = 0; i < bytesRead; i++) {
            temp[i] = messageBytes[i];
          }
          messageBytes = temp;
          aesEncryptedMessage.add(cipher.doFinal(messageBytes));
        } else {
          aesEncryptedMessage.add(cipher.update(messageBytes));
        }
      }
      bis.close();

      // Write AES encrypted digital signature and message combination to file
      writeToFile("message.aescipher", aesEncryptedMessage);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the digit digest and message from the file specified.
   * 
   * @param fileName of the file containing the message
   * @return both the digital digest as well as the original message
   */
  private static Map<String, List<byte[]>> getDigitalDigestAndMessageFromFile(String fileName) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      DigestInputStream dis = new DigestInputStream(new FileInputStream(fileName), md);
      List<byte[]> message = new ArrayList<>();
      int bytesRead;
      while (true) {
        byte[] messageBytes = new byte[1024];
        bytesRead = dis.read(messageBytes, 0, 1024);
        if (bytesRead == -1) {
          break;
        }
        if (bytesRead != 1024) {
          byte[] temp = new byte[bytesRead];
          for (int i = 0; i < bytesRead; i++) {
            temp[i] = messageBytes[i];
          }
          messageBytes = temp;
        }
        message.add(messageBytes);
      }
      dis.close();
      List<byte[]> digitalDigest = new ArrayList<>();
      digitalDigest.add(md.digest());

      Map<String, List<byte[]>> digitalDigestAndMessage = new HashMap<>();
      digitalDigestAndMessage.put("digitalDigest", digitalDigest);
      digitalDigestAndMessage.put("message", message);

      return digitalDigestAndMessage;

    } catch (IOException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Get the symmetric key from the specified file
   * 
   * @param fileName of the file containing the symmetric key
   * @return the retrieved symmetric key
   */
  private static String getSymmetricKeyFromFile(String fileName) {
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream("symmetric.key"));

      String symmetricKey = (String) ois.readObject();

      ois.close();

      return symmetricKey;

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * - Get the private key from the specified file
   * 
   * @param fileName of the file containing the private key
   * @return the retrieved private key
   */
  private static PrivateKey getPrivateKeyFromFile(String fileName) {
    try {
      // Read keys from files
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));

      BigInteger modulus = (BigInteger) ois.readObject();
      BigInteger exponent = (BigInteger) ois.readObject();

      ois.close();

      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, exponent);

      return keyFactory.generatePrivate(privateKeySpec);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Print the given bytes as a hex string
   * 
   * @param input bytes to print
   */
  private static void printHexString(byte[] input) {
    StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < input.length; i++) {
      String hex = Integer.toHexString(0xFF & input[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }

    System.out.println(hexString);
  }

  /**
   * Write the given objects to a file with the given file name
   * 
   * @param fileName of the file in which to write the objects
   * @param byteArrs which should be written to the file
   */
  private static void writeToFile(String fileName, List<byte[]> byteArrs) {
    try {
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));

      // Write each object to file
      for (byte[] bytes : byteArrs) {
        bos.write(bytes);
      }

      bos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
