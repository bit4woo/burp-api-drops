package burp;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.LineListener;

//import org.apache.log4j.Logger;

public class CAESOperator_AES_256 {
//	private final static Logger logger = Logger.getLogger(EncryptUtil.class);
	private static final String CRYPT_METHOD = "AES";
	private static final byte[] IV = "0000000000000000".getBytes();

	/**
	 * AES加密后再base64
	 * 
	 * @param content
	 * @return
	 */
	public static String encrypt(String content) {
		return encrypt(content, getKey());
	}

	private static String encrypt(String content, byte[] bKey) {
		try {
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV);
			SecretKeySpec secretKey = new SecretKeySpec(bKey, CRYPT_METHOD);

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

			byte[] byteContent = padString(content).getBytes("utf-8");
			byte[] result = cipher.doFinal(byteContent);

			return new String(CBase64.encode(result, CBase64.DEFAULT));
		}
		catch (Exception e) {
//			logger.info("Exception:" + e);
		}

		return null;
	}

	public static String decrypt(String content) {
		return decrypt(content, getKey());
	}

	private static String decrypt(String content, byte[] bKey) {
		try {
			// base64 decode
			byte[] bContent = CBase64.decode(content, CBase64.DEFAULT);

			AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV);
			SecretKeySpec secretKey = new SecretKeySpec(bKey, CRYPT_METHOD);
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			// cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] result = cipher.doFinal(bContent);

			return new String(result).trim();
		}
		catch (Exception e) {
//			logger.info("Exception:" + e);
		}

		return null;
	}

	private static String padString(String source) {
		char paddingChar = ' ';
		int size = 16;
		int x;
		try {
			x = source.getBytes("UTF-8").length % size;
		}
		catch (UnsupportedEncodingException e) {
			x = source.length();
		}
		int padLength = size - x;
		StringBuilder sb = new StringBuilder(source);
		for (int i = 0; i < padLength; i++) {
			sb.append(paddingChar);
			// source += paddingChar;
		}

		return sb.toString();// source;
	}

	/**
	 * same as php do.
	 * 
	 * @param input
	 * @return
	 */
	public static String MD5(String input) {
		String result = input;
		if (input != null) {
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
				md.update(input.getBytes());
				BigInteger hash = new BigInteger(1, md.digest());
				result = hash.toString(16);
				while (result.length() < 32) {
					result = "0" + result;
				}
			}
			catch (NoSuchAlgorithmException e) {
//				logger.info("Exception:" + e);
			}
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public static byte[] getKey() {
		//byte[] a = MD5("dye5Cx:O5").getBytes();
		return MD5("dye5Cx:O5").getBytes();
	}
	
	public static void main(String[] args) {
		System.out.println(getKey());
		System.out.println(decrypt("/VWplYZ7tgFzor4LJVow+VsirJfpl0xeep2DKQZFFs78D7vLX+vVOnUbL8GCP9JpJt4W7qdFkJSx9NfHZlETjXGmvqagfoGjT+0cPnZ0bg/MXfG/sqHSzRdqI1uKpZ+/rqIAiuXcLvHCAlkroI9ljb0PEoT711TcHm73TmLs64AEx4g5Zo1k4LaMJLwmMPbS"));
		System.out.println(encrypt("{\"data\":{\"phone\":\"906000004\",\"cpName\":\"teddy\"},\"header\":{\"p2\":\"869011020263403\",\"p3\":\"4.4.1\",\"p17\":\"1.0\"}}"));
		System.out.println(encrypt("{\"code\":0,\"msg\":\"\"}"));
		System.out.println(decrypt("jhhiI7N1yoxKGYY0uWY3mk0ycbjxo5JmOEEur3YIufXzTHc+uJO08Q9Fu7zTOHaQ"));
	}
}
