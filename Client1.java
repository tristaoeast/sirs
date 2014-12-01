import java.net.*;
import java.io.*;

public class Client1
{

	private int _calendar[][][];

	public void Client1(){
		int[][][] _calendar = new int[13][32][24];
	}

   public static void main(String [] args)
   {
   		String serverName = "";
   		int port = 0;

   		if(args.length != 2) {
   			System.out.println("Too few arguments. Run using Client1 [hostname] [port]");
   			System.exit(-1);
   		}
   		else {
      		serverName = args[0];
      		port = Integer.parseInt(args[1]);
      	}

      PasswordHash ph = new PasswordHash();

      System.out.println("What do you want to do?");

      try
      {
         System.out.println("Connecting to " + serverName + " on port " + port);
         //Establish socket connection with server
         Socket client = new Socket(serverName, port);
         System.out.println("Just connected to " + client.getRemoteSocketAddress());
         OutputStream outToServer = client.getOutputStream();
         DataOutputStream out = new DataOutputStream(outToServer);
         out.writeUTF("Hello from " + client.getLocalSocketAddress());
         InputStream inFromServer = client.getInputStream();
         DataInputStream in = new DataInputStream(inFromServer);
         System.out.println("Server says " + in.readUTF());
         client.close();
      }catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}
