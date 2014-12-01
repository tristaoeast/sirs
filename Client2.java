package sirs;

import java.net.*;
import java.io.*;

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

	public static void main(String [] args){

		Client2 _client = new Client2();
		String serverName = args[0];
		String message = "";
		char msg_char = ' ';
		int port = Integer.parseInt(args[1]);
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
