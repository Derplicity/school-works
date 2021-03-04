import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

/**
 * Creates an SSL client who will connect to the server denoted by the given ip/dns and port number;
 * sends and receives info from said server.
 */
public class InfoCollectionClient {

  /**
   * Creates an SSL client who will connect to the server denoted by the given ip/dns and port
   * number; sends and receives info from said server.
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    // Handles missing command line args
    if (args.length == 0 || args[0].isEmpty() || args[1].isEmpty()) {
      throw new Exception(
          "\n| error: missing command line arguments 'ip/dns' and 'port number'\n| usage: java InfoCollectionClient [ip/dns] [port number]");
    }

    String ipOrDns = args[0];
    int portNumber = Integer.parseInt(args[1]);

    // Sets trust store properties
    System.setProperty("javax.net.ssl.trustStore", "3750truststore");
    System.setProperty("javax.net.ssl.trustStorePassword", "123456");

    try {

      // Creates client socket
      SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(ipOrDns, portNumber);

      // Starts client-server handshake
      sslSocket.startHandshake();

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
      InputStream clientInputStream = System.in;
      InputStreamReader clientInputStreamReader = new InputStreamReader(clientInputStream);
      BufferedReader clientBufferedReader = new BufferedReader(clientInputStreamReader);
      String clientInputString = null;

      // Server input stream
      InputStream serverInputStream = sslSocket.getInputStream();
      InputStreamReader serverInputStreamReader = new InputStreamReader(serverInputStream);
      BufferedReader serverBufferedReader = new BufferedReader(serverInputStreamReader);
      String serverInputString = null;

      // Client output stream
      OutputStream clientOutputStream = sslSocket.getOutputStream();
      OutputStreamWriter clientOutputStreamWriter = new OutputStreamWriter(clientOutputStream);
      BufferedWriter clientBufferedWriter = new BufferedWriter(clientOutputStreamWriter);

      while (true) {

        // Listens for and prints server messages
        serverInputString = serverBufferedReader.readLine();

        System.out.println(serverInputString);
        System.out.flush();

        // Ends if server signals socket closing
        if (serverInputString.equals("Bye!"))
          break;

        // Listens for and prints client messages
        clientInputString = clientBufferedReader.readLine();

        clientBufferedWriter.write(clientInputString + '\n');
        clientBufferedWriter.flush();

      }

      clientBufferedReader.close();
      serverBufferedReader.close();
      clientBufferedWriter.close();
      sslSocket.close();

    } catch (Exception exception) {

      exception.printStackTrace();

    }

  }

}
