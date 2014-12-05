import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;


public class Utils 
{
	public static final int NONCE_BYTE_SIZE = 8;
    public static final int IV_BYTE_SIZE = 8;

    private Calendar cal;

    public Utils (){
        cal = new GregorianCalendar();
    }

    public String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0) 
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    public byte[] hexToByteArray(String hex)
    {
        return new BigInteger(hex,16).toByteArray();
    }

	public String generateRandomNonce() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        //Create array for nonce
        byte[] nonce = new byte[NONCE_BYTE_SIZE];
        //Get a random nonce
        sr.nextBytes(nonce);
        //return nonce
        return toHex(nonce);
    }

    public String generateRandomIV() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        //Create array for nonce
        byte[] iv = new byte[IV_BYTE_SIZE];
        //Get a random iv
        sr.nextBytes(iv);
        //return iv
        return toHex(iv);
    }

    public String byteArrayToString(byte[] arr) throws UnsupportedEncodingException{
        return new String(arr,"ISO-8859-1");
    }

    public byte[] stringToByteArray(String str) throws UnsupportedEncodingException{
        return str.getBytes("ISO-8859-1");
    }

    public long getTimeStamp()
    {
        return System.currentTimeMillis();
    }


    public static void main(String[] args)
    {
    	try
    	{
    		Utils utils = new Utils();
    		System.out.println("Genereated nonce successfully: " + utils.generateRandomNonce());
    	}
    	catch(NoSuchProviderException e)
    	{
    		System.err.println("ERROR: " + e);
    	}
    	catch(NoSuchAlgorithmException e)
    	{
    		System.err.println("ERROR: " + e);
    	}

    }

}