import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.math.BigInteger;



public class Utils 
{
	public static final int NONCE_BYTE_SIZE = 4;

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

	public String getNonce() throws NoSuchAlgorithmException, NoSuchProviderException
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

    public static void main(String[] args)
    {
    	try
    	{
    		Utils utils = new Utils();
    		System.out.println("Genereated nonce successfully: " + utils.getNonce());
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