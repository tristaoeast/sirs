import java.net.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Client2
{

	private int _calendar[][][];
 
	public void Client2(){

		int[][][] _calendar = new int[13][32][24];
	}

	public boolean checkCalendarDate(int m, int d, int h){

		if(_calendar[m][d][h] == 1){
			return false;
		}
		else{ 
			_calendar[m][d][h] = 1;
			return true;
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

	public static void main(String [] args){

		Client2 _client = new Client2();
		String message = "";
		char msg_char = ' ';
   		String serverName = "";
   		int port = 0;
   		String correctHash = "1000:aa78d57810a93e7378856693ecabf23fdd33325ec2778ab2:66d9c6667e6c48feb5c709ca0803de5db59ebdaeb29b9b64";

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

		String troll = getInput();

		try
		{

			 System.out.println("Connecting to " + serverName
					     + " on port " + port);
			 Socket socketClient = new Socket(serverName, port);
			 System.out.println("Just connected to "
				      + socketClient.getRemoteSocketAddress());

			 OutputStream outToServer = socketClient.getOutputStream();
			 DataOutputStream out =
				       new DataOutputStream(outToServer);

			 InputStream inFromServer = socketClient.getInputStream();
			 DataInputStream in =
					new DataInputStream(inFromServer);

			 while(!message.equals("ack:yes")){
				 
				 message = in.readUTF();
				 System.out.println("Client1 says " + message);

				 String msgAux = "";
				 int counter = 0;
				 int month = 0, day = 0, hour = 0;
				 for(int i = 0; i < message.length(); i++){

					msg_char = message.charAt(i);
					if(msg_char == '/'){
		
						if(counter == 0){
							day = Integer.parseInt(msgAux);
						}
						else { 
							month = Integer.parseInt(msgAux);
						}
						msgAux = "";
						counter++;
					}
	
					else if(msg_char == '-'){
						msgAux = "";
					}
	
					else {
						msgAux = msgAux + msg_char;
					}
				}
				hour = Integer.parseInt(msgAux);

				if(!_client.checkCalendarDate(month, day, hour)){

					out.writeUTF("No");
				}
				else {
					out.writeUTF("Yes");	
				}

			}
			socketClient.close();
		}
		catch(IOException e){

			e.printStackTrace();
		}
	}
}
