package burp;


import java.util.HashMap;
import java.util.Map; //util包含了很多java中常用的数据类型。
import com.alibaba.fastjson.JSONObject;


public class CString2Other {
	
	public static Map<String,String> MapString2Map(String str) {
	Map<String, String> map = new HashMap<String, String>();
	//String str = "{20130916110808=的非官方大哥,20140306110813=的广告费,20140305165435=二恶太}";
	str = str.substring(1, str.length()-1);//去掉前后括号
	String[] arraydata = str.split(",");//按“，”将其分为字符数组
	for (int i = 0; i < arraydata.length; i++) {
		int j = arraydata[i].indexOf("=");
		map.put(arraydata[i].substring(0, j-1), arraydata[i].substring(j+1, arraydata[i].length()));
		}
	return map;
	}
	
	
	public static JSONObject JSONString2JSONObj(String str){
		JSONObject jsonObject = JSONObject.parseObject(str);
		return jsonObject;
	}
	
   /**
    * json string to map,包含嵌套迭代处理。
    */
    public static Map<String, Object> JSONOString2Map(String jsonStr){
    	Map<String, Object> map = new HashMap<String, Object>(); 
        //最外层解析
        JSONObject json = JSONObject.parseObject(jsonStr);
        for(Object k : json.keySet()){
            Object v = json.get(k); 
            //如果内层还是数组的话，继续解析
            if(v instanceof JSONObject){//JSONArray和JSONObject的区别
            	map.putAll(JSONOString2Map(v.toString()));
            } else {
                map.put(k.toString(), v);
            }
        }
        return map;
    }
    
    
	public static void main(String args[]) throws Exception {
		String test = "{\"order_id\":\"2011608112040003175\",\"pay_info\":{\"method\":\"GET\",\"params\":{\"amt\":\"1.00\",\"body\":\"综合意外险\",\"expiry_time\":\"1440\",\"merchant_code\":\"1512000401\",\"notify_info\":\"{}\",\"notify_url\":\"http://14.29.68.179:8092/receive/order/state/zhongan_pay\",\"order_info\":\"xx\",\"order_type\":\"insurance\",\"out_trade_no\":\"2011608112040003175\",\"pay_channel\":\"alipay^wxpay\",\"request_charset\":\"UTF-8\",\"return_url\":\"https://jr.meizu.com/h5/html/insurance/success.html\",\"sign\":\"7f44a0d33abb99309ffd73ea21bba840\",\"sign_type\":\"MD5\",\"src_type\":\"mobile\",\"subject\":\"综合意外险\"},\"uri\":\"http://cashier.itest.zhongan.com/za-cashier-web/gateway.do\"}}"; 
		JSONObject jO = JSONString2JSONObj(test);
		//System.out.println(jO);
		Map<String,Object> map = JSONOString2Map(test);
		System.out.println(map);
	}
}