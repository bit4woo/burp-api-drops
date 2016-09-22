package burp;

import java.net.URLEncoder;

import com.meizu.bigdata.carina.common.util.*;

class test{
	
	public static void main(String[] args){
		String x;
		try {
			x = TradeEncryptUtil.encrypt("{\"amount\":1,\"channel\":\"APP\",\"device\":\"M95\",\"flymeVer\":\"Flyme OS 5.1.1.0A\",\"goodsDesc\":\"”Œœ∑±“\",\"imei\":\"1234567890123456789\",\"ip\":\"127.0.0.3\",\"prtnCode\":\"meizu_games776\",\"seqNoReq\":\"req160825153016929319058\",\"timestamp\":\"2016-09-19 10:19:00\",\"userAcct\":\"testUser\",\"userId\":1471504498262}");
			System.out.println(x);
			System.out.println(URLEncoder.encode(x));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
}