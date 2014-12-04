import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;

public class Server extends Thread
{
   private ServerSocket serverSocket;
   private TreeMap keys;
   private TreeMap<String, String> addressesMap;
   private TreeMap<String, Long> noncesMap;
   
   
   public Server(int port) throws IOException
   {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(30000);
      addressesMap = new TreeMap<String, String>();
      //the nonce is the key, the timestamp in miliseconds is the value
      noncesMap = new TreeMap<String, Long>();

   }

   public boolean validNounce(String nonce, long currentTimeStamp)
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

   public boolean withinTimeFrame(long currentTimeStamp, long oldTimeStamp)
   {
      if((currentTimeStamp - oldTimeStamp) < 30000)
         return true;
      else
         return false;
   }

   public void run()
   {
      while(true)
      {
         try
         {
            Utils utils = new Utils();
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            // SocketAddress structure: hostname/ip:port
            SocketAddress remoteAddr = server.getRemoteSocketAddress();
            System.out.println("Just connected to " + remoteAddr);
            DataInputStream in = new DataInputStream(server.getInputStream());
            // inMsg structure: User:{Action,User,Nonce,TimeStamp}
            String inMsg = in.readUTF();
            String[] id = inMsg.split(":");
            //TODO: Decrypt messages
            String[] params = null;
            if(id.length == 2)
               params = id[1].split(",");
            else {
               String errorMessage = "ERROR: Wrong message format received. Aborting connection...";
               System.out.println(errorMessage);
               server.close();
               continue;
            }
            if(id[0].equals(params[1])) { //Checks if it is the actual user
               if(params[0].equals("Reg")) {
                  if(params.length == 4) {
                     if((validNounce(params[2], utils.getTimeStamp())) 
                        && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(params[3]))) {
                        
                        //If we reach this point is because everything checks out, so the registration is successful
                        addressesMap.put(id[0], remoteAddr.toString());
                        noncesMap.put(params[2], utils.getTimeStamp());

                     }

                  }

                  // addressesMap.put(id[0],remoteAddr.toString());
                  // noncesMap.add(params[1]);
               }
            }
            else {
               //Wrong credentials. Abort connection
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
