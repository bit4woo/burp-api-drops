package custom;


import java.util.HashMap;
import java.util.Map; //util�����˺ܶ�java�г��õ��������͡�
import com.alibaba.fastjson.JSONObject;


public class String2Other {
	
	public static Map<String,String> MapString2Map(String str) {
	Map<String, String> map = new HashMap<String, String>();
	//String str = "{20130916110808=�ķǹٷ����,20140306110813=�Ĺ���,20140305165435=����̫}";
	str = str.substring(1, str.length()-1);//ȥ��ǰ������
	String[] arraydata = str.split(",");//�������������Ϊ�ַ�����
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
    * json string to map,����Ƕ�׵�������
    */
    public static Map<String, Object> JSONOString2Map(String jsonStr){
    	Map<String, Object> map = new HashMap<String, Object>(); 
        //��������
        JSONObject json = JSONObject.parseObject(jsonStr);
        for(Object k : json.keySet()){
            Object v = json.get(k); 
            //����ڲ㻹������Ļ�����������
            if(v instanceof JSONObject){//JSONArray��JSONObject������
            	map.putAll(JSONOString2Map(v.toString()));
            } else {
                map.put(k.toString(), v);
            }
        }
        return map;
    }
    
    
	public static void main(String args[]) throws Exception {
		String test = "{\"order_id\":\"2011608112040003175\",\"pay_info\":{\"method\":\"GET\",\"params\":{\"amt\":\"1.00\",\"body\":\"�ۺ�������\",\"expiry_time\":\"1440\",\"merchant_code\":\"1512000401\",\"notify_info\":\"{}\",\"notify_url\":\"http://14.29.68.179:8092/receive/order/state/zhongan_pay\",\"order_info\":\"xx\",\"order_type\":\"insurance\",\"out_trade_no\":\"2011608112040003175\",\"pay_channel\":\"alipay^wxpay\",\"request_charset\":\"UTF-8\",\"return_url\":\"https://jr.meizu.com/h5/html/insurance/success.html\",\"sign\":\"7f44a0d33abb99309ffd73ea21bba840\",\"sign_type\":\"MD5\",\"src_type\":\"mobile\",\"subject\":\"�ۺ�������\"},\"uri\":\"http://cashier.itest.zhongan.com/za-cashier-web/gateway.do\"}}"; 
		JSONObject jO = JSONString2JSONObj(test);
		//System.out.println(jO);
		Map<String,Object> map = JSONOString2Map(test);
		System.out.println(map);
	}
}