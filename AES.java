import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
// import java.util.GregorianCalendar;
import java.security.NoSuchAlgorithmException; 
import java.io.UnsupportedEncodingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
 
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

  public SecretKey generateSecretKey()
  {
    // Get the KeyGenerator
    // byte[] raw = null;
    SecretKey skey = null;
    try{
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(256);

      // Generate the secret key specs.
      skey = kgen.generateKey();
      // raw = skey.getEncoded();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return skey;
  }

  public byte[] secretKeyToByteArray(SecretKey skey){
    byte[] raw = skey.getEncoded();
    return raw;
  }


  public void writeKeyToFile(byte[] secretKey, String filename) throws FileNotFoundException,IOException{
    FileOutputStream fos = new FileOutputStream(filename);
    fos.write(secretKey);
    fos.close();
  }

  public byte[] readKeyFromFile(String filename) throws FileNotFoundException,IOException{
    FileInputStream fis = new FileInputStream(filename);
    byte[] secretKey = new byte[32];
    fis.read(secretKey);
    fis.close();
    return secretKey;
  }

  // public byte[] generateSecretKey()
  // {
  //   // Get the KeyGenerator
  //   byte[] raw = null;
  //   try{
  //     KeyGenerator kgen = KeyGenerator.getInstance("AES");
  //     kgen.init(256);

  //     // Generate the secret key specs.
  //     SecretKey skey = kgen.generateKey();
  //     raw = skey.getEncoded();
  //   } catch (NoSuchAlgorithmException e) {
  //     e.printStackTrace();
  //   }

  //   return raw;
  // }

  public void createKeyStore(String keyStoreName){
    
    // store away the keystore
    java.io.FileOutputStream fos = null;
    try {
      KeyStore ks = KeyStore.getInstance("JKS");
      fos = new java.io.FileOutputStream(keyStoreName);
      char[] password = "q2w3e4r5t6y7".toCharArray();
      ks.store(fos, password);
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
        if (fos != null) {
          try{
            fos.close();
          } catch(Exception e){
            e.printStackTrace();
          }
        }
    }
  }
 
  public byte[] encrypt(String plainText, byte[] encryptionKey, String iv) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
    cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv.getBytes("UTF-8")));
    return cipher.doFinal(plainText.getBytes("UTF-8"));
  }
 
  public String decrypt(byte[] cipherText, byte[] encryptionKey, String iv) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
    cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv.getBytes("UTF-8")));
    return new String(cipher.doFinal(cipherText),"UTF-8");
  }

  public static void main(String [] args) {
    try {
      AES aes = new AES();
      Utils utils = new Utils();
      String ptext = "PLAINTEXT";
      String iv = utils.generateRandomIV();
      byte[] secretKey = aes.secretKeyToByteArray(aes.generateSecretKey());
      byte[] secretKeyAlice = aes.secretKeyToByteArray(aes.generateSecretKey());
      byte[] secretKeyBob = aes.secretKeyToByteArray(aes.generateSecretKey());
      System.out.println(secretKeyBob.length);
      aes.writeKeyToFile(secretKeyBob,"BobKeyStore");
      aes.writeKeyToFile(secretKeyAlice,"AliceKeyStore");
      byte[] temp = aes.readKeyFromFile("BobKeyStore");
      byte[] ciphertext = aes.encrypt(ptext, secretKeyBob, iv);
      String deciphertext = aes.decrypt(utils.stringToByteArray(utils.byteArrayToString(ciphertext)),temp,iv);
      System.out.println(deciphertext);


      // aes.createKeyStore("ServerKeyStore");
      // aes.createKeyStore("AliceKeyStore");
      // aes.createKeyStore("BobKeyStore");
      // String iv = "6caa81ea29dd0496";
      // String iv = utils.generateRandomIV();
      // System.out.println(iv);

      // String ptext = "Alice,REG,"+utils.generateRandomNonce()+","+utils.getTimeStamp();
      // System.out.println("plain:   " + ptext);
      

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
 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }

}