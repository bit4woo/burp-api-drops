//this method include 128_192_256
//question: base64在AES中是加密后的最后一个步骤，解密前的第一个步骤吗？
package custom;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AES {
	public static String encrypt(String AESkey, String AESIV, Boolean baseEncode, String AESMode, String plainText)
		    throws Exception
		  {
			System.out.println(AESkey);
			System.out.println(AESIV);
			System.out.println(baseEncode);
			System.out.println(AESMode);//AES/CBC/NoPadding,AES/CBC/PKCS5Padding, AES/ECB/NoPadding, AES/ECB/PKCS5Padding
			System.out.println(plainText);
		    byte[] keyValue = AESkey.getBytes();
		    Key skeySpec = new SecretKeySpec(keyValue, "AES");
		    byte[] iv = AESIV.getBytes();
		    IvParameterSpec ivSpec = new IvParameterSpec(iv);
		    
		    String cmode = AESMode;
		    
		    Cipher cipher = Cipher.getInstance(AESMode);
		    if (cmode.contains("CBC")) {
		      cipher.init(1, skeySpec, ivSpec);
		    } else {
		      cipher.init(1, skeySpec);
		    }
		    byte[] encVal = cipher.doFinal(plainText.getBytes("utf-8"));
		    
		    String encryptedValue = new String(encVal, "UTF-8");
		    //System.out.println(encryptedValue);
		    if (baseEncode) {
		        encryptedValue = (new BASE64Encoder()).encodeBuffer(encVal);
		      }
		    //System.out.println(encryptedValue);
		    return encryptedValue;
		  }
		  
		  public static String decrypt(String AESkey, String AESIV, Boolean baseEncode, String AESMode, String cipherText)
		    throws Exception
		  {
		    byte[] keyValue = AESkey.getBytes();
		    Key skeySpec = new SecretKeySpec(keyValue, "AES");
		    byte[] iv = AESIV.getBytes();
		    IvParameterSpec ivSpec = new IvParameterSpec(iv);
		    
		    String cmode = AESMode;
		    
		    Cipher cipher = Cipher.getInstance(cmode);
		    if (cmode.contains("CBC")) {
		      cipher.init(2, skeySpec, ivSpec);
		    } else {
		      cipher.init(2, skeySpec);
		    }
		    byte[] cipherbytes = cipherText.getBytes();
		    if (baseEncode) {
		      cipherbytes = (new BASE64Decoder()).decodeBuffer(cipherText);
		    }
		    byte[] original = cipher.doFinal(cipherbytes);
		    return new String(original);
		  }

		
		public static void main(String args[]) {
			//String AESkey = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
			String AESkey = "@tony@2015ZEALER";
			//String AESIV = "abcdef1234567890abcdef1234567890";
			String AESIV = "0123456789ABCDEF";
			boolean BaseEncode = false;
			String AESMode = "AES/CBC/PKCS5Padding";
			String Plaintext = "<11pscriptalert(1)11";
			String ciphertext = "kIYpLr53uZfEgkxX8rbovQ==";
			try {
				System.out.println(encrypt(AESkey,AESIV,BaseEncode,AESMode,Plaintext));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				System.out.println(decrypt(AESkey, AESIV, BaseEncode, AESMode, ciphertext));
			} catch (Exception e) {
				// TODO: handle exception
				//e.printStackTrace();
			}
			
			
		}

}
