package custom;

public class HexString2String {
	public static String hexStringToString(String hexstring)
	{
	  int len = hexstring.length();
	  byte[] data = new byte[len / 2];
	  for (int i = 0; i < len; i += 2) {
	    data[(i / 2)] = ((byte)((Character.digit(hexstring.charAt(i), 16) << 4) + Character.digit(hexstring.charAt(i + 1), 16)));
	  }
	  String str = new String(data);
	  return str;
	}
	public static void main(String args[]) {
		System.out.println(hexStringToString("abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"));
		System.out.println(hexStringToString("abcdef1234567890abcdef1234567890"));
		
	}
}
