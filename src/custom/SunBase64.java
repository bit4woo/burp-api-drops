package custom;

import java.io.IOException;
import java.io.PrintWriter;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
/**
 *    BASE64���ܽ���
 *  @author YUANWEi
 */
public class SunBase64 {
    /** 
     * BASE64���� 
     *  
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptBASE64(String key) throws Exception {  
        return (new BASE64Decoder()).decodeBuffer(key);  
    }  
       
    /** 
     * BASE64���� 
     *  
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static String encryptBASE64(byte[] key) throws Exception {  
        return (new BASE64Encoder()).encodeBuffer(key);  
    }
    
    public static void main(String args[]) {
    	String key = "";
    	try {
    		String result = new String((new BASE64Decoder()).decodeBuffer(key));
    		System.out.print(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
