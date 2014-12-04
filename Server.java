import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Server extends Thread
{
   private ServerSocket serverSocket;
   private TreeMap privKeyFiles;
   private TreeMap publicKeyFiles;
   private TreeMap<String, String> addressesMap;
   private TreeMap<String, Long> noncesMap;
   private AES aes;
   private Utils utils;
   
   public Server(int port) throws IOException
   {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(30000);
      addressesMap = new TreeMap<String, String>();
      //the nonce is the key, the timestamp in miliseconds is the value
      noncesMap = new TreeMap<String, Long>();
      aes = new AES();
      utils = new Utils();

   }

   private boolean validNounce(String nonce, long currentTimeStamp) throws IOException
   {
      if(noncesMap.containsKey(nonce)){
         // long tempTimeStamp = (long)noncesMap.get(nonce);
         if(!(withinTimeFrame(currentTimeStamp, noncesMap.get(nonce))))
               noncesMap.remove(nonce);
         return false;
      }
      else
         return true;
   }

   private boolean withinTimeFrame(long currentTimeStamp, long oldTimeStamp)
   {
      if((currentTimeStamp - oldTimeStamp) < 30000)
         return true;
      else
         return false;
   }

   private void wrongFormatMessage(Socket server, DataOutputStream out) throws IOException
   {
      String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
      System.out.println(errorMessage);
      out.writeUTF("Server:Server,ERROR" + errorMessage);
      server.close();
   }

   private void expiredMessage(Socket server, DataOutputStream out) throws IOException
   {
      String errorMessage = "ERROR: Message with expired timstamp pr invalid nonce received. Aborting current connection...";
      System.out.println(errorMessage);
      out.writeUTF("Server:Server,ERROR" + errorMessage);
      server.close();
   }

   private void wrongCredentialsProvided(Socket server, DataOutputStream out) throws IOException
   {
      String errorMessage = "ERROR: Wrong credentials provided. Aborting current connection...";
      System.out.println(errorMessage);
      out.writeUTF("Server:Server,ERROR" + errorMessage);
      server.close();
   }

   private void sendMessage(DataOutputStream out, String plaintext) throws NoSuchAlgorithmException,NoSuchProviderException{
      String iv = utils.generateRandomIV();


      // out.writeUTF("Server:Server,ACKREG," + id[0] + "," + remoteAddr.toString() + "," + params[2] + "," + serverNonce + "," + utils.getTimeStamp());
   }


   public void run()
   {
      while(true)
      {
         try
         {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            // SocketAddress structure: hostname/ip:port
            SocketAddress remoteAddr = server.getRemoteSocketAddress();
            System.out.println("Just connected to " + remoteAddr);
            DataInputStream in = new DataInputStream(server.getInputStream());
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            // inMsg structure: User:{Action,User,Nonce,TimeStamp}
            String inMsg = in.readUTF();
            String[] id = inMsg.split(":");
            //TODO: Decrypt id[1]
            String[] params = null;
            if(id.length == 3)
               params = id[1].split(",");
            else {
               wrongFormatMessage(server, out);
               continue;
            }
            if(id[0].equals(params[0]) && (params.length > 1)) { // Checks if it is the actual user
               
               if(params[1].equals("REG")) { // Checks what action this message performs
                  if(params.length == 4) { // Checks if the  
                     if((validNounce(params[2], utils.getTimeStamp())) 
                        && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(params[3]))) {
                        //If we reach this point is because everything checks out, so the registration is successful
                        addressesMap.put(id[0], remoteAddr.toString());
                        noncesMap.put(params[2], utils.getTimeStamp());
                        String serverNonce = utils.generateRandomNonce();
                        //TODO: encrypt part of the message after ":"
                        sendMessage(out, "Server,ACKREG," + id[0] + "," + remoteAddr.toString() + "," + params[2] + "," + serverNonce + "," + utils.getTimeStamp());
                        
                     }
                     else {
                        expiredMessage(server, out);
                        continue;   
                     }
                  }
                  else {
                     wrongFormatMessage(server, out);
                     continue;
                  }
               }
               else if(params[1].equals("ERROR") && (params.length==3)) { // If an error message is received from the client
                  System.out.println("Client says: " + params[2]);
                  server.close();
                  continue;
               }

            }

            else {
               wrongCredentialsProvided(server, out);
               continue;
            }
            server.close();
         }catch(SocketTimeoutException s)
         {
            System.out.println("Socket timed out!");
            break;
         }catch(IOException e)
         {
            e.printStackTrace();
            break;
         }catch(NoSuchAlgorithmException e)
         {
            e.printStackTrace();
            break;
         }catch(NoSuchProviderException e)
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
