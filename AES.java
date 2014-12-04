import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.GregorianCalendar;
import java.security.NoSuchAlgorithmException; 
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
 
public class AES {
  
  private String initialValue = "6caa81ea29dd0496"; // Generated using nonce generator from Utils.java
  private String plaintext;
  private byte[] encryptionKey;

  private String getInitialValue(){
    return initialValue;
  }

  private String getPlainText(){
    return plaintext;
  }

  private byte[] getEncryptionKey(){
    return encryptionKey;
  }

  public byte[] generateSecretKey()
  {
    // Get the KeyGenerator
    byte[] raw = null;
    try{
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(256);

      // Generate the secret key specs.
      SecretKey skey = kgen.generateKey();
      raw = skey.getEncoded();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return raw;
  }
 
  public byte[] encrypt(String plainText, byte[] encryptionKey, String iv) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
    cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv.getBytes("UTF-8")));
    return cipher.doFinal(plainText.getBytes("UTF-8"));
  }
 
  public String decrypt(byte[] cipherText, byte[] encryptionKey, String iv) throws Exception{
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
    cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv.getBytes("UTF-8")));
    return new String(cipher.doFinal(cipherText),"UTF-8");
  }

  // public static void main(String [] args) {
  //   try {
   //   AES aes = new AES();
    //   encryptionKey = aes.generateSecretKey();

    //   Utils utils = new Utils();
    //   GregorianCalendar cal = new GregorianCalendar();
    //   System.out.println("==Java==");
    //   plaintext = "Alice,REG,"+utils.generateRandomNonce()+","+utils.getTimeStamp();
    //   System.out.println("plain:   " + plaintext);
 
    //   byte[] cipheredData = aes.encrypt(aes.getPlainText(), aes.getEncryptionKey(), aes.initialValue());
    //   System.out.println(cipheredData.length);
    //   // String cipheredMsg = utils.toHex(cipheredData);
    //   // Charset "ISO-8859-1" used to ensure 1-1 Mapping when converting byte[] to String
    //   String cipheredMsg = new String(cipheredData, "ISO-8859-1");
    //   System.out.println("Ciphered text: " + cipheredMsg);

    //   // System.out.print("Ciphered bytes:  ");
    //   // for (int i=0; i<cipheredData.length; i++)
    //   //   System.out.print(new Integer(cipheredData[i])+" ");
    //   // System.out.println("");

    //   // byte[] recvCipheredData = utils.hexToByteArray(cipheredMsg);
    //   byte[] recvCipheredData = cipheredMsg.getBytes("ISO-8859-1");
    //   System.out.println(recvCipheredData.length);
    //   // System.out.print("Received Ciphered data:  ");
    //   // for (int i=0; i<recvCipheredData.length; i++)
    //   //   System.out.print(new Integer(recvCipheredData[i])+" ");
    //   // System.out.println("");
    //   String decrypted = aes.decrypt(recvCipheredData, encryptionKey, initialValue);
 
    //   System.out.println("decrypt: " + decrypted);

    //   if(decrypted.equals(plaintext))
    //     System.out.println("SUCCESS!!! :D");
    //   else
    //     System.out.println("FAILURE... D:");
 
    // } catch (Exception e) {
    //   e.printStackTrace();
    // } 
  // }

}