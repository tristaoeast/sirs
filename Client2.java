import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
//import oracle.security.crypto.core.*;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Client2
{

	private int _calendar[][][];
	//private DiffieHellman _dh;
	private BigInteger _sharedKey;
	private TreeMap<String, Long> noncesMap;
	private AES aes;
	private Utils utils;
	private ServerSocket serverSocket;
	private String _serverName;
	private int _serverPort;
 
	public Client2(int localPort, String serverName, int serverPort) throws IOException, SocketException{

		int[][][] _calendar = new int[13][32][24];
		//_dh = new DiffieHellman();
		noncesMap = new TreeMap<String, Long>();
		aes = new AES();
		utils = new Utils();
		_serverPort = serverPort;
		_serverName = serverName;
		serverSocket = new ServerSocket(localPort);
		serverSocket.setSoTimeout(30000);

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

	public String[] parseMessage(String msg, Socket socketClient, DataOutputStream out) throws IOException, Exception{

		String[] maux1 = msg.split(":");
		String[] maux2 = null;
		String[] parsedMsg = null;
		if(maux1.length == 3){
			String decryptedMsg = aes.decrypt(utils.stringToByteArray(maux1[1]), utils.stringToByteArray(_sharedKey.toString()), maux1[2]);
			maux2 = decryptedMsg.split(",");
		}
		else{
			wrongFormatMessage(socketClient, out);
		}
	
		if(maux1[0].equals(maux2[0]) && (maux2.length > 1)){

			if(maux2[1].equals("CHECK")){

				if(maux2.length == 5){

					if(validNonce(maux2[3], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[4]))){

						parsedMsg = new String[7];
						parsedMsg[0] = maux1[0];
						parsedMsg[1] = maux2[0];
						parsedMsg[2] = maux2[1];
						parsedMsg[3] = maux2[2];
						parsedMsg[4] = maux2[3];
						parsedMsg[5] = maux2[4];
						parsedMsg[6] = maux1[2];
						return parsedMsg;
					}
					else{
						expiredMessage(socketClient, out);
					}
				}
				else{
					wrongFormatMessage(socketClient, out);
				}
			}
			else if(maux2[1].equals("DH")){

				if(maux2[1].equals("Server")){

					if(maux2.length == 7){

						if(validNonce(maux2[5], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[6]))){

							parsedMsg = new String[9];
							parsedMsg[0] = maux1[0];
							parsedMsg[1] = maux2[0];
							parsedMsg[2] = maux2[1];
							parsedMsg[3] = maux2[2];
							parsedMsg[4] = maux2[3];
							parsedMsg[5] = maux2[4];
							parsedMsg[6] = maux2[5];
							parsedMsg[7] = maux2[6];
							parsedMsg[8] = maux1[2];
							return parsedMsg;
						}
						else{
							expiredMessage(socketClient, out);
						}
					}
					else if(maux2.length == 6){

						if(validNonce(maux2[4], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[5]))){

							parsedMsg = new String[8];
							parsedMsg[0] = maux1[0];
							parsedMsg[1] = maux2[0];
							parsedMsg[2] = maux2[1];
							parsedMsg[3] = maux2[2];
							parsedMsg[4] = maux2[3];
							parsedMsg[5] = maux2[4];
							parsedMsg[6] = maux2[5];
							parsedMsg[7] = maux1[2];
							return parsedMsg;
						}
						else{
							expiredMessage(socketClient, out);
						}
					}
					else{
						wrongFormatMessage(socketClient, out);
					}
				}
				else{
					if(maux2.length == 5){

						if(validNonce(maux2[3], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[4]))){

							parsedMsg = new String[7];
							parsedMsg[0] = maux1[0];
							parsedMsg[1] = maux2[0];
							parsedMsg[2] = maux2[1];
							parsedMsg[3] = maux2[2];
							parsedMsg[4] = maux2[3];
							parsedMsg[5] = maux2[4];
							parsedMsg[6] = maux1[2];
							return parsedMsg;
						}
						else{
							expiredMessage(socketClient, out);
						}
					}
					else{
						wrongFormatMessage(socketClient, out);
					}
				}
			}
			else{
				System.out.println("Received unknown type message.");
				socketClient.close();
			}
		}
		else{
			wrongCredentialsProvided(socketClient, out);
		}	
		return parsedMsg;	
	}

	public void establishMeetingDate(Socket server){

		String message = "";
		char msg_char = ' ';
   		String serverName = "";
   		int port = 0;

		try{
			
			OutputStream outToServer = server.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			InputStream inFromServer = server.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			String[] parsedMsg = null;

			while(true){

				message = in.readUTF();
				parsedMsg = parseMessage(message, server, out);
				//System.out.println("Client1 says " + message);

				int month = 0, day = 0, hour = 0;
				String[] split1 = parsedMsg[3].split("-");
				String[] split2 = split1[0].split("/");

				day = Integer.parseInt(split2[0]);
				month = Integer.parseInt(split2[1]);
				hour = Integer.parseInt(split1[1]);

				if(!parsedMsg[3].equals("ack:yes")){
					if(!checkCalendarDate(month, day, hour)){

						String msg = "Bob, CHECK, No," + utils.generateRandomNonce()+", " + utils.getTimeStamp();
						String iv = utils.generateRandomIV();
						out.writeUTF("Bob:" + utils.byteArrayToString(aes.encrypt(msg, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);
					}
					else {
						String msg = "Bob, CHECK, Yes," + utils.generateRandomNonce()+", " + utils.getTimeStamp();
						String iv = utils.generateRandomIV();
						out.writeUTF("Bob:" + utils.byteArrayToString(aes.encrypt(msg, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);
					}
				}
				else{ break;}
			}
			server.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private boolean validNonce(String nonce, long currentTimeStamp) throws IOException{

		if(noncesMap.containsKey(nonce)){
		// long tempTimeStamp = (long)noncesMap.get(nonce);
			if(!(withinTimeFrame(currentTimeStamp, noncesMap.get(nonce))))
				noncesMap.remove(nonce);
			return false;
		}
		else
		return true;
	}

	private boolean withinTimeFrame(long currentTimeStamp, long oldTimeStamp){

		if((currentTimeStamp - oldTimeStamp) < 30000)
			return true;
		else
			return false;
	}

	private void wrongFormatMessage(Socket socketClient, DataOutputStream out) throws IOException{

		String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
		System.out.println(errorMessage);
		out.writeUTF("ALice:Alice,ERROR" + errorMessage);
		socketClient.close();
	}

	private void expiredMessage(Socket socketClient, DataOutputStream out) throws IOException{

		String errorMessage = "ERROR: Message with expired timstamp pr invalid nonce received. Aborting current connection...";
		System.out.println(errorMessage);
		out.writeUTF("Alice:Alice,ERROR" + errorMessage);
		socketClient.close();
	}

	private void wrongCredentialsProvided(Socket socketClient, DataOutputStream out) throws IOException{

		String errorMessage = "ERROR: Wrong credentials provided. Aborting current connection...";
		System.out.println(errorMessage);
		out.writeUTF("Alice:Alice,ERROR" + errorMessage);
		socketClient.close();
	}

	public void createDHPublicValues(Socket socketClient, DataOutputStream out, DataInputStream in){

		try{
			String message = in.readUTF();
			String[] parsedMsg = null;
			BigInteger x, A, p, g, B;
			int bitLength = 1024; // 1024 bits
			SecureRandom rnd = new SecureRandom();
			String Nb;
		
			parsedMsg = parseMessage(message, socketClient, out);

			x = BigInteger.probablePrime(bitLength, rnd);
			p = new BigInteger(parsedMsg[4]);
			g = new BigInteger(parsedMsg[5]);
			B = g.modPow(x, p);


			message = "Bob, DH, Alice, " + B.toString() + ", " + utils.generateRandomNonce()+", " + utils.getTimeStamp();
			String iv = utils.generateRandomIV();
			out.writeUTF("Bob:" + utils.byteArrayToString(aes.encrypt(message, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);

			message = in.readUTF();
			A = new BigInteger(parsedMsg[4]);
			parsedMsg = parseMessage(message, socketClient, out);
			
			_sharedKey = B.modPow(x, p);

			message = in.readUTF();
			parsedMsg = parseMessage(message, socketClient, out);

			Nb = utils.generateRandomNonce();
			message = "Alice, DH, Bob, " + ", " + parsedMsg[4] + ", " + Nb + ", " + utils.getTimeStamp();
			iv = utils.generateRandomIV();
			out.writeUTF("Alice:" + utils.byteArrayToString(aes.encrypt(message, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);

			message = in.readUTF();
			parsedMsg = parseMessage(message, socketClient, out);
			if(!Nb.equals(parsedMsg[4])){

				socketClient.close();
			}

		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void authenticateUser(String correctHash){
		System.out.println("Welcome back, Bob. Please enter your password:");

     		PasswordHash ph = new PasswordHash();
     		char[] passwd;
     		Console cons = System.console();

 		if (cons != null){

 			try {

     			while(!(PasswordHash.validatePassword(passwd = cons.readPassword("[%s]", "Password:"),correctHash)))
      				System.out.println("Wrong password, please try again");
      			java.util.Arrays.fill(passwd, ' ');
      			// System.out.println("Password is clear: " + passwd);

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

	public Socket waitConnection() throws IOException{

		System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
		Socket server = serverSocket.accept();	

		return server;
	}

	public static void main(String [] args){

		String message = "";
		char msg_char = ' ';
   		String serverName = "";
   		int port = 0;
   		int localPort = 0;
   		String correctHash = "1000:aa78d57810a93e7378856693ecabf23fdd33325ec2778ab2:66d9c6667e6c48feb5c709ca0803de5db59ebdaeb29b9b64";

   		if(args.length != 3) {
   			System.err.println("Too few arguments. Run using Client1 [locaPort] [serverHostname] [serverPort]");
   			System.exit(-1);
   		}
   		else {
   				localPort = Integer.parseInt(args[0]);
      			serverName = args[1];
      			port = Integer.parseInt(args[2]);
      		}

		try{
			Client2 client = new Client2(localPort, serverName, port);

	      	authenticateUser(correctHash);

	      	System.out.println("What do you want to do?");

			String troll = getInput();

			Socket server = client.waitConnection();

			client.establishMeetingDate(server);
		}
		catch(SocketException s){
			System.out.println("Socket timed out!");
		}
		catch(IOException e){
			e.printStackTrace();
		}


		/*try
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
		}*/
	}
}
