import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;

public class Server extends Thread
{
   private ServerSocket serverSocket;
   private TreeMap keys;
   private TreeMap addressesMap;
   private TreeSet noncesSet;
   
   
   public Server(int port) throws IOException
   {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(30000);
      addressesMap = new TreeMap<String,String>();
      noncesSet = new TreeSet<String>();

   }

   public void run()
   {
      while(true)
      {
         try
         {
            // Utils utils = new Utils();
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            // SocketAddress structure: hostname/ip:port
            SocketAddress remoteAddr = server.getRemoteSocketAddress();
            System.out.println("Just connected to " + remoteAddr);
            DataInputStream in = new DataInputStream(server.getInputStream());
            // inMsg structure: User:Action:Nonce
            String inMsg = in.readUTF();
            String[] id = inMsg.split(":");
            String[] params = id[1].split(",");
            if(params[0].equals("Reg")){
               // addressesMap.put(id[0],remoteAddr.toString());
               // noncesSet.add(params[1]);
            }

            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            out.writeUTF("Thank you for connecting to "
              + server.getLocalSocketAddress() + "\nGoodbye!");
            server.close();
         }catch(SocketTimeoutException s)
         {
            System.out.println("Socket timed out!");
            break;
         }catch(IOException e)
         {
            e.printStackTrace();
            break;
         }
      }
   }
   public static void main(String [] args)
   {
      int port = 0;
      if(args.length != 1) {
            System.err.println("ERROR: Too few arguments. Run using Server [serverPort]");
            System.exit(-1);
      }
      else
         port = Integer.parseInt(args[0]);
      
      try
      {
         Thread t = new Server(port);
         t.start();
      }catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}
