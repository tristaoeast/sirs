import java.io.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import java.math.BigInteger;

public class RSA
{

	public void generateKeyPair(String userName) throws NoSuchAlgorithmException,InvalidKeySpecException,IOException {

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();
		Key publicKey = kp.getPublic();
		Key privateKey = kp.getPrivate();

		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

		saveToFile(userName+"Public.key", pub.getModulus(), pub.getPublicExponent());
		saveToFile(userName+"Private.key", priv.getModulus(), priv.getPrivateExponent());

	}

	public byte[] publicEncrypt(byte[] data, String userName) 
		throws IOException,NoSuchAlgorithmException,InvalidKeySpecException,IllegalBlockSizeException,
		NoSuchPaddingException,InvalidKeyException,BadPaddingException {

		String keyFileName = "./" + userName + "Public.key";
		PublicKey pubKey = readPublicKeyFromFile(keyFileName);
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}

	public byte[] privateEncrypt(byte[] data, String userName)
		throws IOException,NoSuchAlgorithmException,InvalidKeySpecException,IllegalBlockSizeException,
		NoSuchPaddingException,InvalidKeyException,BadPaddingException {
		
		String keyFileName = "./" + userName + "Private.key";
				System.out.println("KeyFileName:" + keyFileName);
		PrivateKey privKey = readPrivateKeyFromFile(keyFileName);
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, privKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}

	public byte[] publicDecrypt(byte[] data, String userName)
		throws IOException,NoSuchAlgorithmException,InvalidKeySpecException,IllegalBlockSizeException,
		NoSuchPaddingException,InvalidKeyException,BadPaddingException {
		
		String keyFileName = "./" + userName + "Public.key";
		PublicKey pubKey = readPublicKeyFromFile(keyFileName);
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, pubKey);
		byte[] plainData = cipher.doFinal(data);
		return plainData;
	}

	public byte[] privateDecrypt(byte[] data, String userName)
		throws IOException,NoSuchAlgorithmException,InvalidKeySpecException,IllegalBlockSizeException,
		NoSuchPaddingException,InvalidKeyException,BadPaddingException {
		
		String keyFileName = "./" + userName + "Private.key";
		PrivateKey privKey = readPrivateKeyFromFile(keyFileName);
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] plainData = cipher.doFinal(data);
		return plainData;
	}

	public void saveToFile(String fileName, BigInteger mod, BigInteger exp) throws IOException {
  		ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
  		try {
		    oout.writeObject(mod);
		    oout.writeObject(exp);
	  	} catch (Exception e) {
	    	throw new IOException("Unexpected error", e);
	  	} finally {
	    	oout.close();
  		}
	}

	public PublicKey readPublicKeyFromFile(String keyFileName) throws IOException {
	  	InputStream in = RSA.class.getResourceAsStream(keyFileName);
	  	ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
	  	try {
		    BigInteger m = (BigInteger) oin.readObject();
		    BigInteger e = (BigInteger) oin.readObject();
		    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
		    KeyFactory fact = KeyFactory.getInstance("RSA");
		    PublicKey pubKey = fact.generatePublic(keySpec);
		    return pubKey;
		} catch (Exception e) {
		    throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			oin.close();
		}
	}

	public PrivateKey readPrivateKeyFromFile(String keyFileName) throws IOException {
	  	InputStream in = RSA.class.getResourceAsStream(keyFileName);
	  	ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
	  	try {
		    BigInteger m = (BigInteger) oin.readObject();
		    BigInteger e = (BigInteger) oin.readObject();
		    RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
		    KeyFactory fact = KeyFactory.getInstance("RSA");
		    PrivateKey privKey = fact.generatePrivate(keySpec);
		    return privKey;
		} catch (Exception e) {
		    throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			oin.close();
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



	public static void main(String[] args)
	{
		try{
			RSA rsa = new RSA();
			Utils utils = new Utils();
			rsa.generateKeyPair("Alice");
			rsa.generateKeyPair("Server");

			// getInput();
			
			String msg = "Alice,REG,";//+utils.generateRandomNonce()+","+utils.getTimeStamp();
			System.out.println("Message to be encrypted:" + msg);
			byte[] byteMsg = msg.getBytes("UTF-8");
			System.out.println(byteMsg.length);
			byte[] signedData = rsa.privateEncrypt(byteMsg, "Alice");
			System.out.println(signedData.length);
			byte[] ciphSignedData = rsa.publicEncrypt(signedData, "Server");
			String ciphSignedMsg = new String(ciphSignedData, "UTF-8");
			System.out.println("Signed and cipheres data: " + ciphSignedMsg);
			getInput();
			byte[] deciphSignedData = rsa.privateDecrypt(ciphSignedMsg.getBytes("UTF-8"), "Server");
			byte[] deciphData = rsa.publicDecrypt(deciphSignedData, "Alice");
			String deciphMsg = new String(deciphData, "UTF-8");
			System.out.println("Decrypted message: " + deciphMsg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}