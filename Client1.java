import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
//import oracle.security.crypto.core.*;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Client1
{
	private int _calendar[][][];
	//private DiffieHellman _dh;
	private BigInteger _sharedKey;
	private TreeMap<String, Long> noncesMap;
	private AES aes;
	private Utils utils;

	public void Client1(){
		int[][][] _calendar = new int[13][32][24];
		//_dh = new DiffieHellman();
		noncesMap = new TreeMap<String, Long>();
		aes = new AES();
		utils = new Utils();
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

				if(maux2[0].equals("Server")){

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

					if(maux2.length == 6){

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

	public void findCommonDate(Socket socketClient, DataOutputStream out, DataInputStream in){

		int[] dateInterval = new int[4];
		int lastCheckedDay, lastCheckedMonth;

		try{
			String message = "";
			String[] parsedMsg = null;

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
							String msg = "Alice, CHECK, " + Integer.toString(lastCheckedDay) + "/" + Integer.toString(lastCheckedMonth) + "/14-" + Integer.toString(i) + "," + utils.generateRandomNonce()+", " + utils.getTimeStamp();
							String iv = utils.generateRandomIV();
							out.writeUTF("Alice:" + utils.byteArrayToString(aes.encrypt(msg, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);
						}
						message = in.readUTF();
						parsedMsg = parseMessage(message, socketClient, out);
						if(parsedMsg[3].equals("Yes")){ break;}
					}
					if(parsedMsg[3].equals("Yes")){ break;}
					lastCheckedDay++;
				}
				if(parsedMsg[3].equals("Yes")){ break;}
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
							String msg = "Alice, CHECK, " + Integer.toString(lastCheckedDay) + "/" + Integer.toString(lastCheckedMonth) + "/14-" + Integer.toString(i) + "," + utils.generateRandomNonce()+", " + utils.getTimeStamp();
							String iv = utils.generateRandomIV();
							out.writeUTF("Alice:" + utils.byteArrayToString(aes.encrypt(msg, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);
						}
						message = in.readUTF();
						parsedMsg = parseMessage(message, socketClient, out);
						if(parsedMsg[3].equals("Yes")){ break;}
					}
					if(parsedMsg[3].equals("Yes")){ break;}
					lastCheckedDay++;
				}
			}
			if(!parsedMsg[3].equals("Yes")){

				System.out.println("Unable to find common date.");
				out.writeUTF("Alice: Alice, unable to find common date");
			}
			//socketClient.close();
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
			String Na;
		
			parsedMsg = parseMessage(message, socketClient, out);

			x = BigInteger.probablePrime(bitLength, rnd);
			p = new BigInteger(parsedMsg[4]);
			g = new BigInteger(parsedMsg[5]);
			A = g.modPow(x, p);


			message = "Alice, DH, Bob, " + A.toString() + ", " + utils.generateRandomNonce()+", " + utils.getTimeStamp();
			String iv = utils.generateRandomIV();
			out.writeUTF("Alice:" + utils.byteArrayToString(aes.encrypt(message, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);

			message = in.readUTF();
			parsedMsg = parseMessage(message, socketClient, out);
			B = new BigInteger(parsedMsg[4]);

			_sharedKey = B.modPow(x, p);
			
			Na = utils.generateRandomNonce();
			message = "Alice, DH, Bob, " + ", " + Na +", " + utils.getTimeStamp();
			iv = utils.generateRandomIV();
			out.writeUTF("Alice:" + utils.byteArrayToString(aes.encrypt(message, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);

			message = in.readUTF();
			parsedMsg = parseMessage(message, socketClient, out);
			if(!Na.equals(parsedMsg[4])){

				socketClient.close();
			}
			
			message = "Alice, DH, Bob, " + ", " + parsedMsg[5] +", " + utils.getTimeStamp();
			iv = utils.generateRandomIV();
			out.writeUTF("Alice:" + utils.byteArrayToString(aes.encrypt(message, utils.stringToByteArray(_sharedKey.toString()), iv)) + ":" + iv);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Exception e){
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

	try{
		Socket socketClient = new Socket(serverName, port);
		OutputStream outToServer = socketClient.getOutputStream();
		DataOutputStream out = new DataOutputStream(outToServer);
		InputStream inFromServer = socketClient.getInputStream();
		DataInputStream in = new DataInputStream(inFromServer);

		client.createDHPublicValues(socketClient, out, in);
		client.findCommonDate(socketClient, out, in);


		/* System.out.println("Connecting to " + serverName + " on port " + port);
		 //Establish socket connection with server
		 Socket socketClient = new Socket(serverName, port);
		 System.out.println("Just connected to " + client.getRemoteSocketAddress());
		 OutputStream outToServer = client.getOutputStream();
		 DataOutputStream out = new DataOutputStream(outToServer);
		 out.writeUTF("Hello from " + client.getLocalSocketAddress());
		 InputStream inFromServer = client.getInputStream();
		 DataInputStream in = new DataInputStream(inFromServer);
		 System.out.println("Server says " + in.readUTF());*/
		 socketClient.close();
	}
	catch(IOException e){
		e.printStackTrace();
	}
   }
}
