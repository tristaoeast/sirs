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

	public int[] parseDateInput(String interval){

		int[] dateInterval = new int[4];

		String[] split1 = interval.split("-");
		String[] split2 = split1[0].split("/");
		String[] split3 = split1[1].split("/");

		dateInterval[0] = Integer.parseInt(split2[0]);
		dateInterval[1] = Integer.parseInt(split2[1]);
		dateInterval[2] = Integer.parseInt(split3[0]);
		dateInterval[3] = Integer.parseInt(split3[1]);

		return dateInterval;
	}

	public void findCommonDate(){

		int[] dateInterval = new int[4];
		int lastCheckedDay, lastCheckedMonth;
   		String serverName = "";
   		int port = 0;

		try{
			Socket socketClient = new Socket(serverName, port);
			OutputStream outToServer = socketClient.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			String message = "";
			InputStream inFromServer = socketClient.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);

			dateInterval = parseDateInput(getInput());
			lastCheckedDay = dateInterval[0];
			lastCheckedMonth = dateInterval[1];

			while(lastCheckedMonth != dateInterval[3]){

				while(lastCheckedDay < 32){

					int i = 0;
					while(i < 24){

						if(_calendar[lastCheckedMonth][lastCheckedDay][i] != 0){
							i++;
						}
						else{
							out.writeUTF("" + Integer.toString(lastCheckedDay) + "/" + Integer.toString(lastCheckedMonth) + "/14-" + Integer.toString(i));
						}
						message = in.readUTF();
						if(message.equals("Yes")){ break;}
					}
					if(message.equals("Yes")){ break;}
					lastCheckedDay++;
				}
				if(message.equals("Yes")){ break;}
				lastCheckedDay = 1;
				lastCheckedMonth++;		
			}
			if(lastCheckedMonth == dateInterval[3] && !message.equals("Yes")){

				while(lastCheckedDay <= dateInterval[2]){

					int i = 0;
					while(i < 24){

						if(_calendar[lastCheckedMonth][lastCheckedDay][i] != 0){
							i++;
						}
						else{
							out.writeUTF("" + Integer.toString(lastCheckedDay) + "/" + Integer.toString(lastCheckedMonth) + "/14-" + Integer.toString(i));
						}
						message = in.readUTF();
						if(message.equals("Yes")){ break;}
					}
					if(message.equals("Yes")){ break;}
					lastCheckedDay++;
				}
			}
			socketClient.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	private static void authenticateUser(String correctHash){
		System.out.println("Welcome back, Alice. Please enter your password:");

     	PasswordHash ph = new PasswordHash();
     	char[] passwd;
     	Console cons = System.console();
 		if (cons != null){
 			try {
     			while(!(PasswordHash.validatePassword(passwd = cons.readPassword("[%s]", "Password:"),correctHash)))
      				System.out.println("Wrong password, please try again");
      			java.util.Arrays.fill(passwd, ' ');
      			System.out.println("Password is clear: " + passwd);
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
		Client1 client = new Client1();
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

      	authenticateUser(correctHash);

      	System.out.println("What do you want to do?");

	client.findCommonDate();

      /*try
      {
         System.out.println("Connecting to " + serverName + " on port " + port);
         //Establish socket connection with server
         Socket socketClient = new Socket(serverName, port);
         System.out.println("Just connected to " + client.getRemoteSocketAddress());
         OutputStream outToServer = client.getOutputStream();
         DataOutputStream out = new DataOutputStream(outToServer);
         out.writeUTF("Hello from " + client.getLocalSocketAddress());
         InputStream inFromServer = client.getInputStream();
         DataInputStream in = new DataInputStream(inFromServer);
         System.out.println("Server says " + in.readUTF());
         socketClient.close();
      }catch(IOException e)
      {
         e.printStackTrace();
      }*/
   }
}
