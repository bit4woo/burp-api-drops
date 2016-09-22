package custom;

import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import custom.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**AES 是一种可逆加密算法，对用户的敏感信息加密处理
* 对原始数据进行AES加密后，在进行Base64编码转化；
* 可以用如下链接验证计算结果。
* http://www.seacha.com/tools/aes.html?src=11%3C%2Fp%3E%3Cscript%3Ealert%281%2911&mode=CBC&keylen=128&key=%40tony%402015ZEALER&iv=0123456789ABCDEF&bpkcs=pkcs5padding&session=ZDGFuM3GnzMNE0NW518U&aes=b0aef66fa1320ccec6266d471d51d76c&encoding=base64&type=0
*/
public class AES_128 {
	/*
	* 加密用的AESKey 可以用26个字母和数字组成
	* 此处使用AES-128-CBC加密模式，key需要为16位。
	*/
	private String sKey="@tony@2015ZEALER";
	private String ivParameter="0123456789ABCDEF";
	private static AES_128 instance=null;
	public AES_128(){
	
	}
	public static AES_128 getInstance(){
		if (instance==null)
		instance= new AES_128();
		return instance;
	}
	
	public String encrypt(String sSrc) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] raw = sKey.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
		return new String((new BASE64Encoder()).encodeBuffer(encrypted));
	}
	
	
	public String decrypt(String sSrc) throws Exception {
		try {
			byte[] raw = sKey.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] encrypted1 = (new BASE64Decoder()).decodeBuffer(sSrc);//先用base64解密
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original,"utf-8");
			return originalString;
			} catch (Exception ex) {
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		// 需要加密的字符串
		String cSrc = "<11pscriptalert(1)11";
		System.out.println(cSrc);
		String enString = AES_128.getInstance().encrypt(cSrc);
		System.out.println("加密后的字符串:\n" + enString);
		
		// 需要解密的字符串
		enString="uMHIMfrKEytcQ89qacnLk8rgxBCFayD+yBdoxv1usLg=";
		String DeString = AES_128.getInstance().decrypt(enString);
		System.out.println("解密后的字符串:\n" + DeString);
		
		while (true) {
			System.out.println("chose to (de)crypt or (en)crypt");
			Scanner input = new Scanner(System.in);
			String x = input.next();
			//System.out.println(x);
			if (x.equals("de")){
				//System.out.println("decrypt");
				Scanner inputaa = new Scanner(System.in);
				String aa = inputaa.next();
				//String data = "1AB1B76D06A15BF7D83E9629944528D9B1F4F79903E833065DC5447579E2D4AAF25250B8493677DB931CEFC0DE8B09846558F821B65F590118D0F27141EC830C003BCD9039B1962644699BCBAE2E3B81A9CF0F08904A08F2518A2391596B0AF24531ED94EBA061BADBB45FAEFFB63F91B857BB098D9E954A6BCDD8D8CDF701F72550DAFD10D6FD505A6FDACFDBBBB0340238BC905E585C1303401D43625B2C2635903CF4082E5AA949E67C6B49BF95651F93FC2B0394D1F0AF204BCC8CC8E4CA31531728C197475AD843A87F64FA03CCF6E1280A93A6536F9291FAE18D7A9A9D2EF10281291230C0905641D5A35E1D02B1D0F829EABD6B47EC7D5B02186E871018FFFC7E30854CE9C630A32B4C1D0255C02E7F713CB84BBE4CABF34CBE6A984EAD39DE804CCEEB33F1A94DE609E7510CAF1AF157CD6E84E31DC6D2F5ED3C9DA8783AB388AF49E0C67576E8708BCB96A27434E3EA23218E88C01FB7CD3279CE82B2BD8BF6124B6C921486AF2D5A150068E269A6ADF3E60A73FAF87C43E407B0D74FAD96ADC84B7FB53029FDA3BBC21C1DA9E5BB50FDD1F15B184CA51D66419783FCD34502C51FE9AC9CDE158241399DF82AECF305479CAE48A343EB44B4DFC7D524B0BC5F22B27A9EF946BBEA8CD940BF4E2FAAD8938053AB1786FF034036DA72";
				String data = aa;
				String key = "(.2=W$j#z]d3Qx^J";
				System.out.println(AES_128.getInstance().encrypt(cSrc));
			}
			else if (x.equals("en")) {
				//System.out.println("encrypt");
				Scanner inputbb = new Scanner(System.in);
				String bb = inputbb.next();
				String key = "(.2=W$j#z]d3Qx^J";
				System.out.println(AES_128.getInstance().decrypt(enString));
			}
			else{
				System.out.println("error");
			}
				
		}
	}
}

