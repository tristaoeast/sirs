import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Random;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
//import oracle.security.crypto.core.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import javax.crypto.*;
import java.security.*;
import javax.xml.bind.DatatypeConverter;

<<<<<<< HEAD
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
		serverSocket = new ServerSocket(localPort);
		serverSocket.setSoTimeout(30000);

		for(int i = 1; i < 13; i++){

			Random generator = new Random();

			for(int j = 1; j < 32; j++){

				for(int k = 0; k < 24; k++){

					if((k/10) < 1){
						
						_calendar[i][j][k] = 1;
					}
					else{
						boolean res = generator.nextBoolean();
						if(res){
							_calendar[i][j][k] = 0;
						}
						else{
							_calendar[i][j][k] = 1;
						}
					}
				}
			}
		}

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

		Utils utils = new Utils();
		AES aes = new AES();
		String[] maux1 = msg.split(":");
		String[] maux2 = null;
		String[] parsedMsg = null;

		if(maux1.length == 3){

			if(maux1[0].equals("Server")){

				// String decryptedMsg = aes.decrypt(DatatypeConverter.printBase64Binary(maux1[1]), aes.readKeyFromFile("BobKeyStore"), maux1[2]);
				// maux2 = decryptedMsg.split(",");
				maux2 = decryptAndSplitMsg(maux1[1], maux1[2], "Bob");
			}
			else{
				String decryptedMsg = aes.decrypt(DatatypeConverter.parseBase64Binary(maux1[1]), DatatypeConverter.parseBase64Binary(_sharedKey.toString()), maux1[2]);
				maux2 = decryptedMsg.split(",");
			}
		}
		else{
			wrongFormatMessage(socketClient, out);
		    System.out.println("troll1\n");
		}

		if(maux1[0].equals(maux2[0]) && (maux2.length > 1)){
			System.out.println(maux2[1]);
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
					System.out.println("troll2\n");
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
						System.out.println("troll3\n");
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
						System.out.println("troll4\n");
					}
				}
			}
			else{
				System.out.println("Received unknown type message.");
			}
		}
		else{
			wrongCredentialsProvided(socketClient, out);
		}	
		return parsedMsg;	
	}

	public void establishMeetingDate(Socket server){
		Utils utils = new Utils();
		AES aes = new AES();
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

				if(!(parsedMsg == null)){
					//System.out.println("Client1 says " + message);

					int month = 0, day = 0, hour = 0;
					String[] split1 = parsedMsg[3].split("-");
					String[] split2 = split1[0].split("/");

					day = Integer.parseInt(split2[0]);
					month = Integer.parseInt(split2[1]);
					hour = Integer.parseInt(split1[1]);

					if(!parsedMsg[3].equals("ack:yes")){
						if(!checkCalendarDate(month, day, hour)){

							String msg = "Bob,CHECK,No," + utils.generateRandomNonce()+"," + String.valueOf(System.currentTimeMillis());
							String iv = utils.generateRandomIV();
							out.writeUTF("Bob:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKey.toString()), iv)) + ":" + iv);
						}
						else {
							String msg = "Bob,CHECK,Yes," + utils.generateRandomNonce()+"," + String.valueOf(System.currentTimeMillis());
							String iv = utils.generateRandomIV();
							out.writeUTF("Bob:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKey.toString()), iv)) + ":" + iv);
						}
					}
					else{ break;}
				}
				else{
					server.close();
				}
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

	public String encryptAndComposeMsg(String plaintext, String username) throws IllegalBlockSizeException,InvalidKeyException,NoSuchPaddingException,InvalidAlgorithmParameterException,BadPaddingException,NoSuchProviderException,FileNotFoundException,UnsupportedEncodingException,NoSuchAlgorithmException,IOException
   	{
   		AES aes = new AES();
   		Utils utils = new Utils();
      String iv = utils.generateRandomIV();
      // System.out.println("ENTREI!!");
      byte[] cipheredMsg = aes.encrypt(plaintext, aes.readKeyFromFile(username + "KeyStore"), iv);
      return "Bob:" + DatatypeConverter.printBase64Binary(cipheredMsg) + ":" + iv;
   	}

   	public String[] decryptAndSplitMsg(String cipheredMsg, String iv, String username) throws IllegalBlockSizeException,InvalidKeyException,NoSuchAlgorithmException,NoSuchPaddingException,InvalidAlgorithmParameterException,BadPaddingException,IOException,FileNotFoundException,UnsupportedEncodingException
   	{
   		AES aes = new AES();
   		Utils utils = new Utils();
      String decipheredText = aes.decrypt(DatatypeConverter.parseBase64Binary(cipheredMsg), aes.readKeyFromFile(username + "KeyStore"), iv);
      String[] decMsg = decipheredText.split(",");
      return decMsg;
   	}

	public void createDHPublicValues(Socket socketClient, DataOutputStream out, DataInputStream in){

		try{
			Utils utils = new Utils();
			AES aes = new AES();
			String iv;
			String message = in.readUTF();
			String[] parsedMsg = null;
			BigInteger x = null, A, p = null, g, B = null;
			int bitLength = 1024; // 1024 bits
			SecureRandom rnd = new SecureRandom();
			String Nb = null;
			System.out.println("Generating DH public values");
			parsedMsg = parseMessage(message, socketClient, out);

			if(!(parsedMsg == null)){
				x = BigInteger.probablePrime(bitLength, rnd);
				p = new BigInteger(parsedMsg[4]);
				g = new BigInteger(parsedMsg[5]);
				B = g.modPow(x, p);
			
				message = "Bob,DH,Alice," + B.toString() + "," + utils.generateRandomNonce()+"," + String.valueOf(System.currentTimeMillis());
				iv = utils.generateRandomIV();
				out.writeUTF("Bob:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, aes.readKeyFromFile("BobKeyStore"), iv)) + ":" + iv);
				System.out.println("DHC1:1");
			}
			else{
				System.out.println("banana");
				socketClient.close();
			}
			System.out.println("DHC1:2");
			message = in.readUTF();
			parsedMsg = parseMessage(message, socketClient, out);
			
			if(!(parsedMsg == null)){
				A = new BigInteger(parsedMsg[4]);
				_sharedKey = B.modPow(x, p);
			}
			else{
				socketClient.close();
			}
			
			message = in.readUTF();
			System.out.println("DHC1:3");
			parsedMsg = parseMessage(message, socketClient, out);

			if(!(parsedMsg == null)){

				Nb = utils.generateRandomNonce();
				message = "Alice,DH,Bob," + "," + parsedMsg[4] + "," + Nb + "," + String.valueOf(System.currentTimeMillis());
				iv = utils.generateRandomIV();
				out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, DatatypeConverter.parseBase64Binary(_sharedKey.toString()), iv)) + ":" + iv);
			}
			else{
				socketClient.close();
			}

			message = in.readUTF();
			parsedMsg = parseMessage(message, socketClient, out);

			if((!(parsedMsg == null)) || (!Nb.equals(parsedMsg[4]))){

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

		System.out.println("Waiting for connection on port " + serverSocket.getLocalPort() + "...");
		Socket server = serverSocket.accept();

		return server;
	}

	private void registerWithServer(String serverName, int serverPort){
		try{
			Utils utils = new Utils();
			// while(out == null)
			// 	out = new DataOutputStream(outToServer);
			String response = "";
			loop: while(!response.equals("ACKREG")){
					Socket socketClient = new Socket(serverName, serverPort);
					OutputStream outToServer = socketClient.getOutputStream();
					DataOutputStream out = new DataOutputStream(outToServer);
					String testMsg = encryptAndComposeMsg("Bob,REG,"+utils.generateRandomNonce()+","+String.valueOf(System.currentTimeMillis()),"Bob");
					// System.out.println(testMsg); 
					out.writeUTF(testMsg);
					System.out.println("Sent registration to server. Awaiting response...");
					InputStream inFromServer = socketClient.getInputStream();
					DataInputStream in = new DataInputStream(inFromServer);
					String inMsg = in.readUTF();
		            String[] outerMsg = inMsg.split(":");
		            String[] decMsg = null;
		            if(outerMsg.length == 3){
		               decMsg = decryptAndSplitMsg(outerMsg[1], outerMsg[2], "Bob");
		            }
		            else {
		               String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
		               System.out.println(errorMessage);
		               System.out.println("troll5\n");
		               socketClient.close();
		               continue;
		            }
		            if(outerMsg[0].equals(decMsg[0]) && (decMsg.length > 1)) { // Checks if it is the actual user
		               
		               if(decMsg[1].equals("ACKREG")) {
		               	  System.out.println("Registered with server with success!");
		                  socketClient.close();
		                  break loop;
		               } else {
		               	socketClient.close();
		               	continue;
		               }
					}
			}
		} catch (FileNotFoundException e){e.printStackTrace();
		} catch (UnknownHostException e){e.printStackTrace();
		} catch (IOException e){e.printStackTrace();
		} catch (NoSuchAlgorithmException e){e.printStackTrace();
		} catch (NoSuchProviderException e){e.printStackTrace();
		} catch (IllegalBlockSizeException e){e.printStackTrace();
		} catch (InvalidKeyException e){e.printStackTrace();
		} catch (NoSuchPaddingException e){e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e){e.printStackTrace();
		} catch (BadPaddingException e){e.printStackTrace();
		// } catch (IllegalBlockSizeException e){e.printStackTrace();
		// } catch (IllegalBlockSizeException e){e.printStackTrace();
		// } catch (IllegalBlockSizeException e){e.printStackTrace();


		}
	}
	
	public String deparseMessage(String[] message){

		int i;
		String res = null;

		for(i = 1; i < message.length-1; i++){

			if(i == message.length-2){

				res += message[i];
			}
			else{
				res += message[i] + ":";
			}
		}
		return res;
	}

	public static void main(String [] args){

		Client2 bob;
		String message = "";
		char msg_char = ' ';
   		String serverName = "";
   		int port = 0;
   		int localPort = 0;
   		String correctHash = "1000:aa78d57810a93e7378856693ecabf23fdd33325ec2778ab2:66d9c6667e6c48feb5c709ca0803de5db59ebdaeb29b9b64";
   		Utils utils = new Utils();

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
		
			bob = new Client2(localPort, serverName, port);

		    // authenticateUser(correctHash);

		     // String input = "";
	      // 	while(!input.equals("y")){
	      // 		System.out.println("Register with server? [y/n]");
	      // 		input = getInput();
      	// 	}
	      	bob.registerWithServer(serverName, port);
	      	
	      	String response = "";
	      	loop: while(!response.equals("y")){
		      	Socket server = bob.waitConnection();
		      	System.out.println("Meeting scheduling request received from Alice. Do you want to accept it? [y/n]");
		      	response = getInput();
		      	if(response.equals("y")){
					DataOutputStream out = new DataOutputStream(server.getOutputStream());
					DataInputStream in = new DataInputStream(server.getInputStream());
					in.readUTF();
					out.writeUTF(bob.encryptAndComposeMsg("Bob,ACCEPT,Alice,"+utils.generateRandomNonce()+","+String.valueOf(System.currentTimeMillis()),"Bob"));

				bob.createDHPublicValues(server, out, in);
		      		break;
		      	}
	      	}

			//bob.establishMeetingDate(server);
		}
		catch(SocketException s){
			System.out.println("Socket timed out!");
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Exception e){
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
=======
public class Client2 {

    private int _calendar[][][];
    //private DiffieHellman _dh;
    private BigInteger _sharedKey;
    private TreeMap<String, Long> noncesMap;
    private AES aes;
    private Utils utils;
    private ServerSocket serverSocket;
    private String _serverName;
    private int _serverPort;

    public Client2(int localPort, String serverName, int serverPort) throws IOException, SocketException {

        int[][][] _calendar = new int[13][32][24];
        //_dh = new DiffieHellman();
        noncesMap = new TreeMap<String, Long>();
        aes = new AES();
        utils = new Utils();
        serverSocket = new ServerSocket(localPort);
        serverSocket.setSoTimeout(30000);

        for (int i = 1; i < 13; i++) {

            Random generator = new Random();

            for (int j = 1; j < 32; j++) {

                for (int k = 0; k < 24; k++) {

                    if ((k / 10) < 1) {

                        _calendar[i][j][k] = 1;
                    } else {
                        boolean res = generator.nextBoolean();
                        if (res) {
                            _calendar[i][j][k] = 0;
                        } else {
                            _calendar[i][j][k] = 1;
                        }
                    }
                }
            }
        }

    }

    public boolean checkCalendarDate(int m, int d, int h) {

        if (_calendar[m][d][h] == 1) {
            return false;
        } else {
            _calendar[m][d][h] = 1;
            return true;
        }
    }

    public String[] parseMessage(String msg, Socket socketClient, DataOutputStream out) throws IOException, Exception {

        Utils utils = new Utils();
        AES aes = new AES();
        String[] maux1 = msg.split(":");
        String[] maux2 = null;
        String[] parsedMsg = null;

        if (maux1.length == 3) {

            if (maux1[0].equals("Server")) {

                // String decryptedMsg = aes.decrypt(DatatypeConverter.printBase64Binary(maux1[1]), aes.readKeyFromFile("BobKeyStore"), maux1[2]);
                // maux2 = decryptedMsg.split(",");
                maux2 = decryptAndSplitMsg(maux1[1], maux1[2], "Bob");
            } else {
                String decryptedMsg = aes.decrypt(DatatypeConverter.parseBase64Binary(maux1[1]), DatatypeConverter.parseBase64Binary(_sharedKey.toString()), maux1[2]);
                maux2 = decryptedMsg.split(",");
            }
        } else {
            wrongFormatMessage(socketClient, out);
            System.out.println("troll1\n");
        }

        if (maux1[0].equals(maux2[0]) && (maux2.length > 1)) {
            System.out.println(maux2[1]);
            if (maux2[1].equals("CHECK")) {

                if (maux2.length == 5) {

                    if (validNonce(maux2[3], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[4]))) {

                        parsedMsg = new String[7];
                        parsedMsg[0] = maux1[0];
                        parsedMsg[1] = maux2[0];
                        parsedMsg[2] = maux2[1];
                        parsedMsg[3] = maux2[2];
                        parsedMsg[4] = maux2[3];
                        parsedMsg[5] = maux2[4];
                        parsedMsg[6] = maux1[2];
                        return parsedMsg;
                    } else {
                        expiredMessage(socketClient, out);
                    }
                } else {
                    wrongFormatMessage(socketClient, out);
                    System.out.println("troll2\n");
                }
            } else if (maux2[1].equals("DH")) {

                if (maux2[0].equals("Server")) {

                    if (maux2.length == 7) {

                        if (validNonce(maux2[5], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[6]))) {

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
                        } else {
                            expiredMessage(socketClient, out);
                        }
                    } else if (maux2.length == 6) {

                        if (validNonce(maux2[4], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[5]))) {

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
                        } else {
                            expiredMessage(socketClient, out);
                        }
                    } else {
                        wrongFormatMessage(socketClient, out);
                        System.out.println("troll3\n");
                    }
                } else {
                    if (maux2.length == 5) {

                        if (validNonce(maux2[3], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[4]))) {

                            parsedMsg = new String[7];
                            parsedMsg[0] = maux1[0];
                            parsedMsg[1] = maux2[0];
                            parsedMsg[2] = maux2[1];
                            parsedMsg[3] = maux2[2];
                            parsedMsg[4] = maux2[3];
                            parsedMsg[5] = maux2[4];
                            parsedMsg[6] = maux1[2];
                            return parsedMsg;
                        } else {
                            expiredMessage(socketClient, out);
                        }
                    } else {
                        wrongFormatMessage(socketClient, out);
                        System.out.println("troll4\n");
                    }
                }
            } else {
                System.out.println("Received unknown type message.");
            }
        } else {
            wrongCredentialsProvided(socketClient, out);
        }
        return parsedMsg;
    }

    public void establishMeetingDate(Socket server) {
        Utils utils = new Utils();
        AES aes = new AES();
        String message = "";
        char msg_char = ' ';
        String serverName = "";
        int port = 0;

        try {

            OutputStream outToServer = server.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = server.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            String[] parsedMsg = null;

            while (true) {

                message = in.readUTF();
                parsedMsg = parseMessage(message, server, out);

                if (!(parsedMsg == null)) {
                    //System.out.println("Client1 says " + message);

                    int month = 0, day = 0, hour = 0;
                    String[] split1 = parsedMsg[3].split("-");
                    String[] split2 = split1[0].split("/");

                    day = Integer.parseInt(split2[0]);
                    month = Integer.parseInt(split2[1]);
                    hour = Integer.parseInt(split1[1]);

                    if (!parsedMsg[3].equals("ack:yes")) {
                        if (!checkCalendarDate(month, day, hour)) {

                            String msg = "Bob,CHECK,No," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                            String iv = utils.generateRandomIV();
                            out.writeUTF("Bob:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKey.toString()), iv)) + ":" + iv);
                        } else {
                            String msg = "Bob,CHECK,Yes," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                            String iv = utils.generateRandomIV();
                            out.writeUTF("Bob:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKey.toString()), iv)) + ":" + iv);
                        }
                    } else {
                        break;
                    }
                } else {
                    server.close();
                }
            }
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validNonce(String nonce, long currentTimeStamp) throws IOException {

        if (noncesMap.containsKey(nonce)) {
            // long tempTimeStamp = (long)noncesMap.get(nonce);
            if (!(withinTimeFrame(currentTimeStamp, noncesMap.get(nonce))))
                noncesMap.remove(nonce);
            return false;
        } else
            return true;
    }

    private boolean withinTimeFrame(long currentTimeStamp, long oldTimeStamp) {

        if ((currentTimeStamp - oldTimeStamp) < 30000)
            return true;
        else
            return false;
    }

    private void wrongFormatMessage(Socket socketClient, DataOutputStream out) throws IOException {

        String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
        System.out.println(errorMessage);
        out.writeUTF("ALice:Alice,ERROR" + errorMessage);
        socketClient.close();
    }

    private void expiredMessage(Socket socketClient, DataOutputStream out) throws IOException {

        String errorMessage = "ERROR: Message with expired timstamp pr invalid nonce received. Aborting current connection...";
        System.out.println(errorMessage);
        out.writeUTF("Alice:Alice,ERROR" + errorMessage);
        socketClient.close();
    }

    private void wrongCredentialsProvided(Socket socketClient, DataOutputStream out) throws IOException {

        String errorMessage = "ERROR: Wrong credentials provided. Aborting current connection...";
        System.out.println(errorMessage);
        out.writeUTF("Alice:Alice,ERROR" + errorMessage);
        socketClient.close();
    }

    public String encryptAndComposeMsg(String plaintext, String username) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, NoSuchProviderException, FileNotFoundException, UnsupportedEncodingException, NoSuchAlgorithmException, IOException {
        AES aes = new AES();
        Utils utils = new Utils();
        String iv = utils.generateRandomIV();
        // System.out.println("ENTREI!!");
        byte[] cipheredMsg = aes.encrypt(plaintext, aes.readKeyFromFile(username + "KeyStore"), iv);
        return "Bob:" + DatatypeConverter.printBase64Binary(cipheredMsg) + ":" + iv;
    }

    public String[] decryptAndSplitMsg(String cipheredMsg, String iv, String username) throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IOException, FileNotFoundException, UnsupportedEncodingException {
        AES aes = new AES();
        Utils utils = new Utils();
        String decipheredText = aes.decrypt(DatatypeConverter.parseBase64Binary(cipheredMsg), aes.readKeyFromFile(username + "KeyStore"), iv);
        String[] decMsg = decipheredText.split(",");
        return decMsg;
    }

    public void createDHPublicValues(Socket socketClient, DataOutputStream out, DataInputStream in) {

        try {
            Utils utils = new Utils();
            AES aes = new AES();
            String iv;
            String message = in.readUTF();
            String[] parsedMsg = null;
            BigInteger x = null, A, p = null, g, B = null;
            int bitLength = 1024; // 1024 bits
            SecureRandom rnd = new SecureRandom();
            String Nb = null;
            System.out.println("Generating DH public values");
            parsedMsg = parseMessage(message, socketClient, out);

            if (!(parsedMsg == null)) {
                x = BigInteger.probablePrime(bitLength, rnd);
                p = new BigInteger(parsedMsg[4]);
                g = new BigInteger(parsedMsg[5]);
                B = g.modPow(x, p);

                message = "Bob,DH,Alice," + B.toString() + "," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                iv = utils.generateRandomIV();
                out.writeUTF("Bob:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, aes.readKeyFromFile("BobKeyStore"), iv)) + ":" + iv);
            } else {
                System.out.println("banana");
                socketClient.close();
            }
            message = in.readUTF();
            parsedMsg = parseMessage(message, socketClient, out);

            if (!(parsedMsg == null)) {
                A = new BigInteger(parsedMsg[4]);
                _sharedKey = B.modPow(x, p);
            } else {
                socketClient.close();
            }

            message = in.readUTF();
            parsedMsg = parseMessage(message, socketClient, out);

            if (!(parsedMsg == null)) {

                Nb = utils.generateRandomNonce();
                message = "Alice,DH,Bob," + "," + parsedMsg[4] + "," + Nb + "," + String.valueOf(System.currentTimeMillis());
                iv = utils.generateRandomIV();
                out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, DatatypeConverter.parseBase64Binary(_sharedKey.toString()), iv)) + ":" + iv);
            } else {
                socketClient.close();
            }

            message = in.readUTF();
            parsedMsg = parseMessage(message, socketClient, out);

            if ((!(parsedMsg == null)) || (!Nb.equals(parsedMsg[4]))) {

                socketClient.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void authenticateUser(String correctHash) {
        System.out.println("Welcome back, Bob. Please enter your password:");

        PasswordHash ph = new PasswordHash();
        char[] passwd;
        Console cons = System.console();

        if (cons != null) {

            try {

                while (!(PasswordHash.validatePassword(passwd = cons.readPassword("[%s]", "Password:"), correctHash)))
                    System.out.println("Wrong password, please try again");
                java.util.Arrays.fill(passwd, ' ');
                // System.out.println("Password is clear: " + passwd);

            } catch (NoSuchAlgorithmException e) {
                System.out.println("ERROR: " + e);
            } catch (InvalidKeySpecException e) {
                System.out.println("ERROR: " + e);
            }
        } else {
            System.err.println("No console.");
            System.exit(1);
        }
    }

    private static String getInput() {

        String s = "";

        try {

            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            s = bufferRead.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public Socket waitConnection() throws IOException {

        System.out.println("Waiting for connection on port " + serverSocket.getLocalPort() + "...");
        Socket server = serverSocket.accept();

        return server;
    }

    private void registerWithServer(String serverName, int serverPort) {
        try {
            Utils utils = new Utils();
            // while(out == null)
            //  out = new DataOutputStream(outToServer);
            String response = "";
            loop: while (!response.equals("ACKREG")) {
                Socket socketClient = new Socket(serverName, serverPort);
                OutputStream outToServer = socketClient.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String testMsg = encryptAndComposeMsg("Bob,REG," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis()), "Bob");
                // System.out.println(testMsg);
                out.writeUTF(testMsg);
                System.out.println("Sent registration to server. Awaiting response...");
                InputStream inFromServer = socketClient.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String inMsg = in.readUTF();
                String[] outerMsg = inMsg.split(":");
                String[] decMsg = null;
                if (outerMsg.length == 3) {
                    decMsg = decryptAndSplitMsg(outerMsg[1], outerMsg[2], "Bob");
                } else {
                    String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
                    System.out.println(errorMessage);
                    System.out.println("troll5\n");
                    socketClient.close();
                    continue;
                }
                if (outerMsg[0].equals(decMsg[0]) && (decMsg.length > 1)) { // Checks if it is the actual user

                    if (decMsg[1].equals("ACKREG")) {
                        System.out.println("Registered with server with success!");
                        socketClient.close();
                        break loop;
                    } else {
                        socketClient.close();
                        continue;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
            // } catch (IllegalBlockSizeException e){e.printStackTrace();
            // } catch (IllegalBlockSizeException e){e.printStackTrace();
            // } catch (IllegalBlockSizeException e){e.printStackTrace();


        }
    }

    public String deparseMessage(String[] message) {

        int i;
        String res = null;

        for (i = 1; i < message.length - 1; i++) {

            if (i == message.length - 2) {

                res += message[i];
            } else {
                res += message[i] + ":";
            }
        }
        return res;
    }

    public static void main(String [] args) {

        Client2 bob;
        String message = "";
        char msg_char = ' ';
        String serverName = "";
        int port = 0;
        int localPort = 0;
        String correctHash = "1000:aa78d57810a93e7378856693ecabf23fdd33325ec2778ab2:66d9c6667e6c48feb5c709ca0803de5db59ebdaeb29b9b64";
        Utils utils = new Utils();

        if (args.length != 3) {
            // System.err.println("Too few arguments. Run using Client1 [locaPort] [serverHostname] [serverPort]");
            // System.exit(-1);
            localPort = 8081;
            port = 8080;
            serverName = "localhost";
        } else {
            localPort = Integer.parseInt(args[0]);
            serverName = args[1];
            port = Integer.parseInt(args[2]);
        }

        bob = null;
        try {
            bob = new Client2(localPort, serverName, port);
            bob.registerWithServer(serverName, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // authenticateUser(correctHash);

        // String input = "";
        //    while(!input.equals("y")){
        //        System.out.println("Register with server? [y/n]");
        //        input = getInput();
        //  }


        String response = "";
        loop: while (true) {
            try {
                Socket server = bob.waitConnection();
                System.out.println("Meeting scheduling request received from Alice. Do you want to accept it? [y/n]");
                response = getInput();
                if (response.equals("y")) {
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    in.readUTF();
                    out.writeUTF(bob.encryptAndComposeMsg("Bob,ACCEPT,Alice," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis()), "Bob"));
                    bob.createDHPublicValues(server, out, in);
                    continue loop;
                } else if (response.equals("n")) {
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    in.readUTF();
                    out.writeUTF(bob.encryptAndComposeMsg("Bob,REJECT,Alice," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis()), "Bob"));
                    server.close();
                    continue loop;
                }
            } catch (SocketException s) {
                System.out.println("Socket timed out!");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                System.out.println("Input must be a number");
                continue loop;
            } catch (Exception e) {
                e.printStackTrace();
            }


            //bob.establishMeetingDate(server);
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
>>>>>>> c7f5abeeb92903e8c4d68daf5abe7ba9eb0ba56e
}
