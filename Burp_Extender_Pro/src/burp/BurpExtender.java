package burp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.PrintWriter;
import java.net.URLEncoder;


import custom.AES_128; //AES加解密算法的实现类
import custom.JsonParser;
import custom.MD5;
import custom.MapSort;
import custom.Unicode; //unicode解码的实现类

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meizu.bigdata.carina.common.util.TradeEncryptUtil;


public class BurpExtender implements IBurpExtender, IHttpListener
{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    
    private PrintWriter stdout;//现在这里定义变量，再在registerExtenderCallbacks函数中实例化，如果都在函数中就只是局部变量，不能在这实例化，因为要用到其他参数。
    private String ExtenderName = "AES encrypter for carina";
    private List<String> paraWhiteList = new ArrayList<String>();
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
    	stdout = new PrintWriter(callbacks.getStdout(), true);
    	stdout.println(ExtenderName);
        this.callbacks = callbacks;
        helpers = callbacks.getHelpers();
        callbacks.setExtensionName(ExtenderName); //插件名称
        callbacks.registerHttpListener(this); //如果没有注册，下面的processHttpMessage方法是不会生效的。处理请求和响应包的插件，这个应该是必要的
    }

    @Override
    public void processHttpMessage(int toolFlag,boolean messageIsRequest,IHttpRequestResponse messageInfo)
    {
    	if (toolFlag == (toolFlag&checkEnabledFor())){
    		//不同的toolflag代表了不同的burp组件 https://portswigger.net/burp/extender/api/constant-values.html#burp.IBurpExtenderCallbacks
        	Encrypter(messageIsRequest, messageInfo);
        	//ReSign(messageIsRequest, messageInfo);
    	}
    }
    
    public void Encrypter(boolean messageIsRequest,IHttpRequestResponse messageInfo){
		if (messageIsRequest){ //对请求包进行处理
			
			int flag = 3;//控制处理模块，参数，消息体，http头
			IRequestInfo analyzeRequest;
			
			switch (flag) {
			case 1://处理对象 是参数的情况
			{
				analyzeRequest = helpers.analyzeRequest(messageInfo); //对消息体进行解析
    			byte[] new_Request = messageInfo.getRequest();
    			//the method of get parameter
    			List<IParameter> paraList = analyzeRequest.getParameters();//当body是json格式的时候，这个方法也可以正常获取到键值对，牛掰。但是PARAM_JSON等格式不能通过updateParameter方法来更新。
                //如果在url中的参数的值是 xxx=json格式的字符串 这种形式的时候，getParameters应该是无法获取到最底层的键值对的。
    			for (IParameter para : paraList){// 循环获取参数，判断类型，进行加密处理后，再构造新的参数，合并到新的请求包中。
    				if ((para.getType() == 0 || para.getType() == 1) && !paraWhiteList.contains(para.getName())){ //getTpe()就是来判断参数是在那个位置的，cookie中的参数是不需要进行加密处理的。还要排除白名单中的参数。
	    				//参数共有7种格式，0是url参数，1是body参数，2是cookie参数 （helpers.updateParameter只支持这三种），6是json参数--这种参数的更新用更新body的方法。
    					String key = para.getName(); //获取参数的名称
	    				String value = para.getValue(); //获取参数的值
	    				//stdout.println(key+":"+value);
	    				String encryptedValue;
						try {
							encryptedValue = TradeEncryptUtil.encrypt(value);
							encryptedValue = URLEncoder.encode(encryptedValue); //还要进行URL编码，否则会出现= 等特殊字符导致参数判断异常;
		    				stdout.println(key+":"+value+":"+encryptedValue); //输出到extender的UI窗口，可以让使用者有一些判断
		    				IParameter newPara = helpers.buildParameter(key, encryptedValue, para.getType()); //构造新的参数
		    				new_Request = helpers.updateParameter(new_Request, newPara); //构造新的请求包
		    				messageInfo.setRequest(new_Request);//设置最终新的请求包
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}
    			}
				break;
			}
			case 2://处理对象是header的情况
			{
    			//the method of get header
				analyzeRequest = helpers.analyzeRequest(messageInfo); //前面的操作可能已经修改了请求包，所以做后续的更改前，都需要从新获取。
				List<String> headers = analyzeRequest.getHeaders();
    			break;
			}
    		case 3:
    		{
    			//the method of get body
    			analyzeRequest = helpers.analyzeRequest(messageInfo); //前面的操作可能已经修改了请求包，所以做后续的更改前，都需要从新获取。
    			List<String> headers = analyzeRequest.getHeaders();//签名header可能已经改变，需要重新获取
    			int bodyOffset = analyzeRequest.getBodyOffset();
    			byte[] byte_Request = messageInfo.getRequest();
    			String request = new String(byte_Request); //byte[] to String
                String body = request.substring(bodyOffset);
                byte[] byte_body = body.getBytes();  //String to byte[]
                
				try {
					String newBody = TradeEncryptUtil.encrypt(body);
					stdout.println(newBody);
					byte[] bodyByte = newBody.getBytes();
	    			byte[] new_Request = helpers.buildHttpMessage(headers, bodyByte); //如果修改了header或者数修改了body，不能通过updateParameter，使用这个方法。
	    			messageInfo.setRequest(new_Request);//设置最终新的请求包
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
			default:
				/* to verify the updated result
				for (IParameter para : helpers.analyzeRequest(messageInfo).getParameters()){
					stdout.println(para.getValue());
				}
				*/
				break;
			}			
		}
		else{//处理返回，响应包
			IResponseInfo analyzedResponse = helpers.analyzeResponse(messageInfo.getResponse()); //getResponse获得的是字节序列
			short statusCode = analyzedResponse.getStatusCode();
			List<String> header = analyzedResponse.getHeaders();
			String resp = new String(messageInfo.getResponse());
			int bodyOffset = analyzedResponse.getBodyOffset();//响应包是没有参数的概念的，大多需要修改的内容都在body中
            String body = resp.substring(bodyOffset);
			
			int flag = 2;
			
			switch (flag) {
			case 1://处理header，如果这里修改了response,注意case2中应该从新获取header内容。
				break;
			case 2://处理body
    			if (statusCode==200){
    				try{
	                    String deBody= TradeEncryptUtil.decrypt(body);
	                    deBody = deBody.replace("\"", "\\\"");
	                    String UnicodeBody = (new Unicode()).unicodeDecode(deBody);
	                    String newBody = body +"\r\n" +UnicodeBody; //将新的解密后的body附到旧的body后面
	                    //String newBody = UnicodeBody;
	                    byte[] bodybyte = newBody.getBytes();
	                    messageInfo.setResponse(helpers.buildHttpMessage(header, bodybyte));
    				}catch(Exception e){
    					stdout.println(e);
    				}
    			}
				break;
			default:
				break;
			}
		}	    		
    }
    
    public void ReSign(boolean messageIsRequest,IHttpRequestResponse messageInfo){
    	if (messageIsRequest){ //对请求包进行处理
			
			//获取各种参数和消息体的方法罗列如下，无非三种，body，header，paramater
			IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo); //对消息体进行解析 
			//the method of get header
			List<String> headers = analyzeRequest.getHeaders(); //获取http请求头的信息，返回可以看作是一个python中的列表，java中是叫泛型什么的，还没弄清楚
			//the method of get body
			int bodyOffset = analyzeRequest.getBodyOffset();
			byte[] byte_Request = messageInfo.getRequest();
			String request = new String(byte_Request); //byte[] to String
            String body = request.substring(bodyOffset);
            byte[] byte_body = body.getBytes();  //String to byte[]
			//the method of get parameter
            List<IParameter> paras = analyzeRequest.getParameters();
            Map<String, String> paraMap = new HashMap<>();
            
            String signPara = "sign";
            byte signParaType = 0;
            
            int flag = 1;
            
            switch (flag) {
			case 1://针对直接可以获取的url参数和body参数，不包括json型参数
			{
				byte[] new_Request = messageInfo.getRequest();
				for (IParameter para:paras){
					String key = para.getName();
					String value = para.getValue();
					if (!paraWhiteList.contains(key)&&(para.getType()==1 ||para.getType()==0)){//注意这里和para_encrypter模块中使用的是同一个白名单
						paraMap.put(key, value);
					}
					if (key.equals(signPara)){
						signParaType = para.getType();
					}
				}
				//paraMap.put("key","secretkey");//1.将secret key的键值与参数一样对待 
				paraMap = MapSort.sortMapByKey(paraMap, "ASCENDING");
				String paraString = MapSort.combineMapEntry(paraMap, "&");
				paraString += "&key=secretkey";//2.将secret key直接附加到合并的字符串末尾
				String newSign = MD5.GetMD5Code(paraString);
				
				IParameter newPara = helpers.buildParameter("sign", newSign, signParaType); //构造新的参数,如果参数是PARAM_JSON类型，这个方法是不适用的
				new_Request = helpers.updateParameter(new_Request, newPara); //构造新的请求包，这里是方法一updateParameter
				messageInfo.setRequest(new_Request);
				break;
			}
			case 2://针对json型参数。
			{
				byte[] new_Request = messageInfo.getRequest();
				//如果在url中的参数的值是 xxx=json格式的字符串 这种形式的时候，getParameters应该是无法获取到最底层的键值对的。想要更新其中的参数也需要使用如下的方法。
				paraMap = JsonParser.parseJson(body);
	            
				
				paraMap = MapSort.sortMapByKey(paraMap, "ASCENDING");
				String paraString = MapSort.combineMapEntry(paraMap, "&");
				paraString += "&key=secretkey";//2.将secret key直接附加到合并的字符串末尾
				String newSign = MD5.GetMD5Code(paraString);
	            
    			JSONObject jsonObject = JSON.parseObject(body);
    			//JSONObject header = jsonObject.getJSONObject("header");多层结构
    			jsonObject.replace("sign", newSign);
    			body = JSON.toJSONString(jsonObject);
    			byte_body = body.getBytes();
				new_Request = helpers.buildHttpMessage(headers, byte_body); //如果修改了header或者数修改了body，而不是通过updateParameter，使用这个方法。
				messageInfo.setRequest(new_Request);
			}
			default:
				break;
			}
    	}
    }
    
	public int checkEnabledFor(){
		//get values that should enable this extender for which Component.
		//不同的toolflag代表了不同的burp组件 https://portswigger.net/burp/extender/api/constant-values.html#burp.IBurpExtenderCallbacks
		int status = 0;
		int	TOOL_COMPARER = 512;
		int	TOOL_DECODER = 256;
		int	TOOL_EXTENDER = 1024;
		int	TOOL_INTRUDER = 32;
		int	TOOL_PROXY = 4;
		int	TOOL_REPEATER = 64;
		int	TOOL_SCANNER = 16;
		int	TOOL_SEQUENCER = 128;
		int	TOOL_SPIDER = 8;
		int	TOOL_SUITE = 1;
		int	TOOL_TARGET= 2;
		
		return status+TOOL_PROXY+TOOL_REPEATER+TOOL_SCANNER+TOOL_INTRUDER;
	}
}