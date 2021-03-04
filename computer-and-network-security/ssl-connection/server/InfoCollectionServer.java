import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Creates an SSL server on the given port; creates user info files for each client who connects.
 */
public class InfoCollectionServer {

  /**
   * Recursively deletes a directory and all of its files.
   * 
   * @param dir
   */
  public static void deleteDir(File dir) {
    for (File subFile : dir.listFiles()) {
      if (subFile.isDirectory()) {
        deleteDir(subFile);
      } else {
        subFile.delete();
      }
    }
    dir.delete();
  }

  /**
   * Creates an SSL server on the given port; creates user info files for each client who connects.
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    // Handles missing command args
    if (args.length == 0 || args[0].isEmpty()) {
      throw new Exception(
          "\n| error: missing command line argument 'port number'\n| usage: java InfoCollectionServer [port number]");
    }

    int portNumber = Integer.parseInt(args[0]);

    // Sets key store properties
    System.setProperty("javax.net.ssl.keyStore", "3750keystore");
    System.setProperty("javax.net.ssl.keyStorePassword", "123456");

    try {

      // Creates/Replaces output directory
      File outputDir = new File("output");
      if (outputDir.exists()) {
        deleteDir(outputDir);
      }
      outputDir.mkdir();

      // Creates server socket
      SSLServerSocketFactory sslServerSocketFactory =
          (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
      SSLServerSocket sslServerSocket =
          (SSLServerSocket) sslServerSocketFactory.createServerSocket(portNumber);

      while (true) {

        // Accepts connection from client
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

        // The client thread
        Thread clientThread = new Thread(new Runnable() {
          @Override
          public void run() {

            try {

              // Session details
              SSLSession sslSocketSession = sslSocket.getSession();
              String peerHost = sslSocketSession.getPeerHost();
              String cypherSuite = sslSocketSession.getCipherSuite();
              String protocol = sslSocketSession.getProtocol();
              byte[] sessionID = sslSocketSession.getId();
              long creationTime = sslSocketSession.getCreationTime();
              long lastAccessTime = sslSocketSession.getLastAccessedTime();

              // Prints session details
              System.out.printf("Peer host is %s\n", peerHost);
              System.out.printf("Cypher suite is %s\n", cypherSuite);
              System.out.printf("Protocol is %s\n", protocol);
              System.out.print("Session ID is ");
              for (byte b : sessionID) {
                System.out.printf("%02X", b);
              }
              System.out.println();
              System.out.printf("The creation time of this session is %d\n", creationTime);
              System.out.printf("The last accessed time of this session is %d\n", lastAccessTime);

              // Client input stream
              InputStream clientInputStream = sslSocket.getInputStream();
              InputStreamReader clientInputStreamReader = new InputStreamReader(clientInputStream);
              BufferedReader clientBufferedReader = new BufferedReader(clientInputStreamReader);

              // Server output stream
              OutputStream serverOutputStream = sslSocket.getOutputStream();
              OutputStreamWriter serverOutputStreamWriter =
                  new OutputStreamWriter(serverOutputStream);
              BufferedWriter serverBufferedWriter = new BufferedWriter(serverOutputStreamWriter);
              String clientInputString = null;

              int userID = 1;

              while (true) {

                // Creates thread directory in output directory
                File threadDir = new File(outputDir.getPath() + '/' + "client-"
                    + String.valueOf(Thread.currentThread().getId()));
                threadDir.mkdir();

                // Creates user file in thread directory
                FileOutputStream fos = new FileOutputStream(
                    new File(threadDir.getPath() + '/' + "user" + userID + ".txt"));

                // START: Server questions and client response file writes

                serverBufferedWriter.write("User Name:" + '\n');
                serverBufferedWriter.flush();

                clientInputString = clientBufferedReader.readLine();
                fos.write(("User Name: " + clientInputString + '\n').getBytes());

                serverBufferedWriter.write("Full Name:" + '\n');
                serverBufferedWriter.flush();

                clientInputString = clientBufferedReader.readLine();
                fos.write(("Full Name: " + clientInputString + '\n').getBytes());

                serverBufferedWriter.write("Address:" + '\n');
                serverBufferedWriter.flush();

                clientInputString = clientBufferedReader.readLine();
                fos.write(("Address: " + clientInputString + '\n').getBytes());

                serverBufferedWriter.write("Phone number:" + '\n');
                serverBufferedWriter.flush();

                clientInputString = clientBufferedReader.readLine();
                fos.write(("Phone number: " + clientInputString + '\n').getBytes());

                serverBufferedWriter.write("Email address:" + '\n');
                serverBufferedWriter.flush();

                clientInputString = clientBufferedReader.readLine();
                fos.write(("Email address: " + clientInputString + '\n').getBytes());

                // END: Server questions and client response file writes

                fos.close();
                userID++;

                // Checks whether client has more users to add
                serverBufferedWriter.write("Add more users? (yes or any for no)" + '\n');
                serverBufferedWriter.flush();

                clientInputString = clientBufferedReader.readLine();
                if (!clientInputString.toLowerCase().equals("yes"))
                  break;

              }

              // Signals to client that the socket is closing
              serverBufferedWriter.write("Bye!" + '\n');
              serverBufferedWriter.flush();

              clientBufferedReader.close();
              serverBufferedWriter.close();
              sslSocket.close();

            } catch (Exception exception) {

              exception.printStackTrace();

            }

          }
        });

        // Starts client thread
        clientThread.start();

      }

    } catch (Exception exception) {

      exception.printStackTrace();

    }

  }
}
