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
   private TreeMap<String, SocketAddress> addressesMap;
   private TreeMap<String, Long> noncesMap;
   private AES aes;
   private Utils utils;
   
   public Server(int port) throws IOException
   {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(30000);
      addressesMap = new TreeMap<String, SocketAddress>();
      //the nonce is the key, the timestamp in miliseconds is the value
      noncesMap = new TreeMap<String, Long>();
      aes = new AES();
      utils = new Utils();

   }

   private boolean validNonce(String nonce, long currentTimeStamp) throws IOException
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

   private void wrongFormatMessage(Socket server, DataOutputStream out, String username) throws IOException,FileNotFoundException,UnsupportedEncodingException,Exception
   {
      String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
      System.out.println(errorMessage);
      out.writeUTF(encryptAndComposeMsg("Server,ERROR,"+errorMessage+","+utils.generateRandomNonce()+","+utils.getTimeStamp(),username));
      server.close();
   }

   private void expiredMessage(Socket server, DataOutputStream out, String username) throws IOException,FileNotFoundException,UnsupportedEncodingException,Exception
   {
      String errorMessage = "ERROR: Message with expired timestamp or invalid nonce received. Aborting current connection...";
      System.out.println(errorMessage);
      out.writeUTF(encryptAndComposeMsg("Server,ERROR,"+errorMessage+","+utils.generateRandomNonce()+","+utils.getTimeStamp(),username));
      server.close();
   }

   private void wrongCredentialsProvided(Socket server, DataOutputStream out, String username) throws IOException,FileNotFoundException,UnsupportedEncodingException,Exception
   {
      String errorMessage = "ERROR: Wrong credentials provided. Aborting current connection...";
      System.out.println(errorMessage);
      out.writeUTF(encryptAndComposeMsg("Server,ERROR,"+errorMessage+","+utils.generateRandomNonce()+","+utils.getTimeStamp(),username));
      server.close();
   }

   private String[] decryptAndSplitMsg(String cipheredMsg, String iv, String username) throws FileNotFoundException,UnsupportedEncodingException,Exception
   {
      String decipheredText = aes.decrypt(utils.stringToByteArray(cipheredMsg), aes.readKeyFromFile(username + "KeyStore"), iv);
      String[] decMsg = decipheredText.split(",");
      return decMsg;
   }

   private String encryptAndComposeMsg(String plaintext, String username) throws FileNotFoundException,UnsupportedEncodingException,Exception
   {
      String iv = utils.generateRandomIV();
      byte[] cipheredMsg = aes.encrypt(plaintext, aes.readKeyFromFile(username + "KeyStore"), iv);
      return "Server:" + utils.byteArrayToString(cipheredMsg) + ":" + iv;
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
            String[] outerMsg = inMsg.split(":");
            String[] decMsg = null;
            if(outerMsg.length == 3){
               decMsg = decryptAndSplitMsg(outerMsg[1], outerMsg[2], outerMsg[0]);
            }
            else {
               String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
               System.out.println(errorMessage);
               // out.writeUTF(encryptAndComposeMsg("Server,ERROR,"+errorMessage+","+utils.generateRandomNonce()+","+utils.getTimeStamp(),username));
               server.close();
               continue;
            }
            if(outerMsg[0].equals(decMsg[0]) && (decMsg.length > 1)) { // Checks if it is the actual user
               
               if(decMsg[1].equals("REG")) { // Checks what action this message performs
                  System.out.println(outerMsg[0] + " wishes to register its address with the server...");
                  if(decMsg.length == 4) { // Checks if the  
                     if((validNonce(decMsg[2], utils.getTimeStamp())) 
                        && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(decMsg[3]))) {
                        //If we reach this point is because everything checks out, so the registration is successful
                        addressesMap.put(outerMsg[0], remoteAddr);
                        noncesMap.put(decMsg[2], utils.getTimeStamp());
                        String serverNonce = utils.generateRandomNonce();
                        System.out.println(outerMsg[0]+" registered successfully with address " + remoteAddr);
                        out.writeUTF(encryptAndComposeMsg("Server,ACKREG,"+outerMsg[0]+","+remoteAddr.toString()+","+serverNonce+","+utils.getTimeStamp(), outerMsg[0]));
                        server.close();
                        continue;
                        
                     }
                     else {expiredMessage(server, out, outerMsg[0]);continue;}
                  }
                  else {wrongFormatMessage(server, out, outerMsg[0]);continue;}
               }
               else if(decMsg[1].equals("REQ")) { // Checks what action this message performs
                  if(decMsg.length == 5) { // Checks if the
                     System.out.print(outerMsg[0] + " wishes to schedule a meeting with " + decMsg[decMsg.length-3]);
                     if((validNonce(decMsg[decMsg.length-2], utils.getTimeStamp())) 
                        && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(decMsg[decMsg.length-1]))) {
                        //If we reach this point is because everything checks out, so the server notifies the other user of the schedule request
                        String serverNonce = utils.generateRandomNonce();
                        
                        String[] strsplit1 = addressesMap.get(decMsg[decMsg.length-3]).toString().split(":");
                        String[] strsplit2 = strsplit1[0].split("/");
                        int bobPort = Integer.parseInt(strsplit1[1]);
                        String bobHostname = strsplit2[1];

                        Socket bob = new Socket(bobHostname, bobPort);
                        DataOutputStream outToBob = new DataOutputStream(bob.getOutputStream());
                        DataInputStream inFromBob = new DataInputStream(bob.getInputStream());

                        outToBob.writeUTF(encryptAndComposeMsg("Server,REQ,"+outerMsg[0]+","+serverNonce+","+utils.getTimeStamp(), decMsg[decMsg.length-3]));
                        System.out.println("Scheduling request from " + outerMsg[0] + " successfully sent to " + decMsg[decMsg.length-3]);
                        
                        // Now the server waits for the acceptance or rejection of the request for a meeting schedule and forwards the response to the requester
                        String responseMsg = inFromBob.readUTF();
                        String[] rOuterMsg = responseMsg.split(":");
                        String[] rDecMsg = null;
                        if(rOuterMsg.length == 3){
                           rDecMsg = decryptAndSplitMsg(rOuterMsg[1], rOuterMsg[2], rOuterMsg[0]);
                        }
                        else {
                           String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
                           System.out.println(errorMessage);
                           // out.writeUTF(encryptAndComposeMsg("Server,ERROR,"+errorMessage+","+utils.generateRandomNonce()+","+utils.getTimeStamp(),username));
                           server.close();
                           bob.close();
                           continue;
                        }

                        if(rOuterMsg[0].equals(rDecMsg[0]) && (rDecMsg.length > 1)) { // Checks if it is the actual user
                           if(rDecMsg[1].equals("ACCEPT")) { // Checks what action this message performs
                              if(rDecMsg.length == 5) { // Checks if the  
                                 System.out.println(rOuterMsg[0] + " accepted the request to schedule a meeting from " + rDecMsg[rDecMsg.length-3]);
                                 if((validNonce(rDecMsg[rDecMsg.length-2], utils.getTimeStamp())) 
                                    && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(rDecMsg[rDecMsg.length-1]))) {
                                    //If we reach this point is because everything checks out, so we forward the acceptance message
                                    out.writeUTF(encryptAndComposeMsg("Server,ACCEPT,"+rOuterMsg[0]+","+bobHostname+","+bobPort+","+utils.generateRandomNonce()+","+utils.getTimeStamp(), rDecMsg[rDecMsg.length-3]));
                                    server.close();
                                    bob.close();
                                    System.out.println("Forwarded successfully " + rOuterMsg[0]+ " acceptance of " + rDecMsg[rDecMsg.length-3] + " request to schedule a meeting");
                                    continue;
                                 }
                                 else {expiredMessage(server, out, rOuterMsg[0]);continue;}
                              }
                              else {wrongFormatMessage(server, out, rOuterMsg[0]);continue;}
                           }
                           else if(rDecMsg[1].equals("REJECT")) { // Checks what action this message performs
                              if(rDecMsg.length == 5) { // Checks if the  
                                 System.out.println(rOuterMsg[0] + " rejected the request to schedule a meeting from " + rDecMsg[rDecMsg.length-3]);
                                 if((validNonce(rDecMsg[rDecMsg.length-2], utils.getTimeStamp())) 
                                    && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(rDecMsg[rDecMsg.length-1]))) {
                                    //If we reach this point is because everything checks out, so we forward the acceptance message
                                    out.writeUTF(encryptAndComposeMsg("Server,REJECT,"+rOuterMsg[0]+","+utils.generateRandomNonce()+","+utils.getTimeStamp(), rDecMsg[rDecMsg.length-3]));
                                    server.close();
                                    bob.close();
                                    System.out.println("Forwarded successfully " + rOuterMsg[0]+ " rejection of " + rDecMsg[rDecMsg.length-3] + " request to schedule a meeting");
                                    continue;
                                 }
                                 else {expiredMessage(server, out, rOuterMsg[0]);continue;}
                              }
                              else {wrongFormatMessage(server, out, rOuterMsg[0]);continue;}
                           }
                           else {wrongFormatMessage(server, out, rOuterMsg[0]);continue;}
                        }
                        else {wrongCredentialsProvided(server, out, rOuterMsg[0]);continue;}
                     }
                     else {expiredMessage(server, out, outerMsg[0]);continue;}
                  }
                  else {wrongFormatMessage(server, out, outerMsg[0]);continue;}
               }

               else if(decMsg[1].equals("ERROR") && (decMsg.length==5)) { // If an error message is received from the client
                  if((validNonce(decMsg[decMsg.length-2], utils.getTimeStamp())) 
                     && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(decMsg[decMsg.length-1]))){
                     System.out.println("Client says: " + decMsg[2]);
                     server.close();
                     continue;
                  }
               }

            }

            else {
               wrongCredentialsProvided(server, out, outerMsg[0]);
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
         }catch(Exception e){
            e.printStackTrace();
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
