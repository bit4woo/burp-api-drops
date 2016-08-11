package burp;

import java.io.PrintWriter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CJson {
	public static void main(String[] args) {
		String jsonstring = "{\"code\":200,\"message\":\"\",\"redirect\":\"\",\"value\":\"B3589E62EA52EBBA2830F9ADA73206B9\"}";
		JSONObject jb = JSONObject.fromObject(jsonstring);
		String ja = jb.getString("value");
		System.out.println(jb);
		System.out.println(ja);	
	}

}