import java.net.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Client1
{

	private int _calendar[][][];

	public void Client1(){
		int[][][] _calendar = new int[13][32][24];
	}

	private static String getInput(){
		String s = "";
		try{
        	BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        	s = bufferRead.readLine();
    	}
    	catch(IOException e)
    	{
       	 	e.printStackTrace();
    	}
    	return s;
	}

   public static void main(String [] args)
   {
   		String serverName = "";
   		int port = 0;
   		String correctHash = "1000:4ef06e4a486a7f279ee1fb67b9f66ff1ad0c1fc1da22e8f7:1cb66a2517552374390797c200ff32205a457184e4ba4cf5";

   		if(args.length != 2) {
   			System.err.println("Too few arguments. Run using Client1 [serverHostname] [serverPort]");
   			System.exit(-1);
   		}
   		else {
      		serverName = args[0];
      		port = Integer.parseInt(args[1]);
      	}

      	System.out.println("Welcome back, Alice. Please enter your password:");

     	PasswordHash ph = new PasswordHash();
     	char[] passwd;
     	Console cons = System.console();
 		if (cons != null){
 			try {
     			while(!(PasswordHash.validatePassword(passwd = cons.readPassword("[%s]", "Password:"),correctHash)))
      				System.out.println("Wrong password, please try again");
      			java.util.Arrays.fill(passwd, ' ');
      		}
        	catch(NoSuchAlgorithmException e)
        	{
           		System.out.println("ERROR: " + e);
        	}
        	catch(InvalidKeySpecException e)
        	{
            	System.out.println("ERROR: " + e);
        	}
      	}
      	else {
      		System.err.println("No console.");
            System.exit(1);
      	}
      	

      	System.out.println("What do you want to do?");
      	
      	String troll = getInput();

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
