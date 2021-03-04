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
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Receiver {
  public static void main(String[] args) {

    try {
      // Get needed keys from files
      PublicKey publicKey = getPublicKeyFromFile("XPublic.key");
      String symmetricKey = getSymmetricKeyFromFile("symmetric.key");

      // Get the name of the file to write the original message to
      Scanner scanner = new Scanner(System.in);
      System.out.print("Input the name of the message file: ");
      String fileName = scanner.nextLine();
      scanner.close();

      // Decrypt the digital signature combined with the original message via AES Decryption
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream("message.aescipher"));
      List<byte[]> aesDecryptedMessage = new ArrayList<>();
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(symmetricKey.getBytes("UTF-8"), "AES"),
          new IvParameterSpec("AAAAAAAAAAAAAAAA".getBytes("UTF-8")));
      int bytesRead;
      while (true) {
        byte[] messageBytes = new byte[16];
        bytesRead = bis.read(messageBytes, 0, 16);
        if (bytesRead == -1) {
          break;
        }
        if (bis.available() == 0) {
          aesDecryptedMessage.add(cipher.doFinal(messageBytes));
        } else {
          aesDecryptedMessage.add(cipher.update(messageBytes));
        }
      }

      bis.close();

      // Write the decrypted digital signature combined with the original message to file
      writeToFile("message.ds-msg", aesDecryptedMessage);

      // Decrypt the digital digest from the digital signature via RSA Decryption
      bis = new BufferedInputStream(new FileInputStream("message.ds-msg"));
      cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.DECRYPT_MODE, publicKey);
      byte[] ds = new byte[128];
      bis.read(ds, 0, 128);
      byte[] dd = cipher.doFinal(ds);

      // Write the digital digest to file and print as hex string
      List<byte[]> bytesToAdd = new ArrayList<>();
      bytesToAdd.add(dd);
      writeToFile("message.dd", bytesToAdd);
      printHexString(dd);

      // Get original message from the decrypted digital signature/message file
      List<byte[]> message = new ArrayList<>();
      while (true) {
        byte[] messageBytes = new byte[1024];
        bytesRead = bis.read(messageBytes, 0, 1024);
        if (bytesRead == -1)
          break;
        if (bytesRead != 1024) {
          byte[] temp = new byte[bytesRead];
          for (int i = 0; i < bytesRead; i++) {
            temp[i] = messageBytes[i];
          }
          messageBytes = temp;
        }
        message.add(messageBytes);
      }

      // Write the original message to the user provided file
      writeToFile(fileName, message);

      bis.close();

      // Calculate the digital digest of the original message received
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      DigestInputStream dis = new DigestInputStream(new FileInputStream(fileName), md);
      while (true) {
        byte[] messageBytes = new byte[1024];
        bytesRead = dis.read(messageBytes, 0, 1024);
        if (bytesRead == -1) {
          break;
        }
      }
      dis.close();
      byte[] digitalDigest = md.digest();

      // Print the calculated
      printHexString(digitalDigest);

      // Print whether the authentication check was passed
      if (Arrays.equals(digitalDigest, dd)) {
        System.out.println("The digital digest passed the authentication check.");
      } else {
        System.out.println("The digital digest failed the authentication check.");
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
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
   * Get the symmetric key from the specified file
   * 
   * @param fileName of the file containing the symmetric key
   * @return the symmetric key
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
   * Get the public key from the specified file
   * 
   * @param fileName of the file containing the public key
   * @return the public key
   */
  private static PublicKey getPublicKeyFromFile(String fileName) {
    try {
      // Read keys from files
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));

      BigInteger modulus = (BigInteger) ois.readObject();
      BigInteger exponent = (BigInteger) ois.readObject();

      ois.close();

      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);

      return keyFactory.generatePublic(publicKeySpec);

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
