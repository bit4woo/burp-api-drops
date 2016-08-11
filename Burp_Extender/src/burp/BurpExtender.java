package burp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.PrintWriter;
import java.net.URLEncoder;


import burp.CAESOperator; //AES加解密算法的实现类
import burp.CUnicode; //unicode解码的实现类
import burp.IParameter;


public class BurpExtender implements IBurpExtender, IHttpListener
{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PrintWriter stdout;//现在这里定义变量，再在registerExtenderCallbacks函数中实例化，如果都在函数中就只是局部变量，不能在这实例化，因为要用到其他参数。
    
    // implement IBurpExtender
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
    	stdout = new PrintWriter(callbacks.getStdout(), true);
    	//PrintWriter stdout = new PrintWriter(callbacks.getStdout(), true); 这种写法是定义变量和实例化，这里的变量就是新的变量而不是之前class中的全局变量了。
    	//stdout.println("testxx");
    	//System.out.println("test"); 不会输出到burp的
        this.callbacks = callbacks;
        helpers = callbacks.getHelpers();
        callbacks.setExtensionName("AES encrypt Java edition"); //插件名称
        callbacks.registerHttpListener(this); //如果没有注册，下面的processHttpMessage方法是不会生效的。处理请求和响应包的插件，这个应该是必要的
    }

    @Override
    public void processHttpMessage(int toolFlag,boolean messageIsRequest,IHttpRequestResponse messageInfo)
    {
		List<String> paraWhiteList = new ArrayList<String>(); //参数白名单，白名单中的参数值不进行加密计算
		Map<String, String> ParaMap = new HashMap<String, String>();//很类似，写在一起;这个map很类是python中的dict。
		paraWhiteList.add("android");
		
    	if (toolFlag == 64 || toolFlag == 16 || toolFlag == 32 || toolFlag == 4){ //不同的toolflag代表了不同的burp组件 https://portswigger.net/burp/extender/api/constant-values.html#burp.IBurpExtenderCallbacks
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
                List<IParameter> paraList = analyzeRequest.getParameters();//当body是json格式的时候，这个方法也可以正常获取到键值对，牛掰。但是PARAM_JSON等格式不能通过updateParameter方法来更新。
                //如果在url中的参数的值是 xxx=json格式的字符串 这种形式的时候，getParameters应该是无法获取到最底层的键值对的。
                //获取各种参数和消息体部分的集合 
                
                
                //判断一个请求是否是文件上传的请求。
    			boolean isFileUploadRequest =false;
    			for (String header : headers){
    				//stdout.println(header);
    				if (header.toLowerCase().indexOf("content-type")!=-1 && header.toLowerCase().indexOf("boundary")!=-1){//通过http头中的内容判断这个请求是否是文件上传的请求
    					isFileUploadRequest = true;
    				}
    			}
    			
    			//*******************encrypt each parameter with AES **********************//
    			if (isFileUploadRequest == false){ //对文件上传的请求，对其中的参数不做加密处理
	    			byte[] new_Request = messageInfo.getRequest();
	    			for (IParameter para : paraList){// 循环获取参数，判断类型，进行加密处理后，再构造新的参数，合并到新的请求包中。
	    				if ((para.getType() == 0 || para.getType() == 1) && !paraWhiteList.contains(para.getName())){ 
	    					//getTpe()就是来判断参数是在那个位置的，cookie中的参数是不需要进行加密处理的。还要排除白名单中的参数。
		    				//这里要注意的是，参数的类型共6种，如果body中的参数是json或者xml格式，需要单独判断。
	    					String key = para.getName(); //获取参数的名称
		    				String value = para.getValue(); //获取参数的值
		    				//stdout.println(key+":"+value);
		    				CAESOperator aes = new CAESOperator(); //实例化加密的类
		    				String aesvalue;
		    				aesvalue = aes.encrypt(value); //对value值进行加密
		    				aesvalue = URLEncoder.encode(aesvalue); //还要进行URL编码，否则会出现= 等特殊字符导致参数判断异常
		    				stdout.println(key+":"+value+":"+aesvalue); //输出到extender的UI窗口，可以让使用者有一些判断
		    				//更新包的方法集合
		    				//更新参数
		    				IParameter newPara = helpers.buildParameter(key, aesvalue, para.getType()); //构造新的参数,如果参数是PARAM_JSON类型，这个方法是不适用的
		    				//IParameter newPara = helpers.buildParameter(key, aesvalue, PARAM_BODY); //要使用这个PARAM_BODY 是不是需要先实例化IParameter类。
		    				new_Request = helpers.updateParameter(new_Request, newPara); //构造新的请求包
		    				// new_Request = helpers.buildHttpMessage(headers, byte_body); //如果修改了header或者数修改了body，而不是通过updateParameter，使用这个方法。
	    				}
	    			}
	    			messageInfo.setRequest(new_Request);//设置最终新的请求包
    			}
    			/* to verify the updated result
    			for (IParameter para : helpers.analyzeRequest(messageInfo).getParameters()){
    				stdout.println(para.getValue());
    			}
    			*/
    			//*******************encrypt each parameter with AES **********************//
    			
    			//*******************recalculate sign**************************//
    			if (isFileUploadRequest == false){ //对某些请求不做处理，可以在这里控制
	    			byte[] new_Request = messageInfo.getRequest();
	    			for (IParameter para : paraList){// 循环获取参数，判断类型，进行加密处理后，再构造新的参数，合并到新的请求包中。
	    				if (!paraWhiteList.contains(para.getName())){//不在白名单中的参数就放到ParaMap中，也就是需要进行sign计算的参数列表。
	    					String key = para.getName(); //获取参数的名称
		    				String value = para.getValue(); //获取参数的值
		    				ParaMap.put(key, value);
		    				//stdout.println(key+":"+value);
		    				CRecalculater resign = new CRecalculater(); //实例化加密的类
		    				String newSign;
		    				newSign = resign.sign(ParaMap); //对value值进行加密
		    				stdout.println("New Sign:"+newSign); //输出到extender的UI窗口，可以让使用者有一些判断
		    				//更新包的方法集合
		    				//更新参数
		    				IParameter newPara = helpers.buildParameter(key, aesvalue, para.getType()); //构造新的参数,如果参数是PARAM_JSON类型，这个方法是不适用的
		    				//IParameter newPara = helpers.buildParameter(key, aesvalue, PARAM_BODY); //要使用这个PARAM_BODY 是不是需要先实例化IParameter类。
		    				new_Request = helpers.updateParameter(new_Request, newPara); //构造新的请求包，这里是方法一updateParameter
		    				// new_Request = helpers.buildHttpMessage(headers, byte_body); //如果修改了header或者数修改了body，而不是通过updateParameter，使用这个方法。
		    				
		    				//当参数是在json数据格式中的时候，需要用到如下方法；
		    				//如果在url中的参数的值是 xxx=json格式的字符串 这种形式的时候，getParameters应该是无法获取到最底层的键值对的。想要更新其中的参数也需要使用如下的方法。
//			    			JSONObject jsonObject = JSON.parseObject(body);
//			    			JSONObject header = jsonObject.getJSONObject("header");
//			    			header.replace("sign", sign);
//			    			jsonObject.replace("header", header);
//			    			body = JSON.toJSONString(jsonObject);
		    				
	    				}
	    			}
	    			messageInfo.setRequest(new_Request);//设置最终新的请求包
    			}
    			/* to verify the updated result
    			for (IParameter para : helpers.analyzeRequest(messageInfo).getParameters()){
    				stdout.println(para.getValue());
    			}
    			*/
    			
    		}
    		
    		else{
    			//处理返回，响应包
    			IResponseInfo analyzedResponse = helpers.analyzeResponse(messageInfo.getResponse()); //getResponse获得的是字节序列
    			List<String> header = analyzedResponse.getHeaders();
    			short statusCode = analyzedResponse.getStatusCode();
    			int bodyOffset = analyzedResponse.getBodyOffset();
    			if (statusCode==200){
    				try{
	    				CAESOperator aes = new CAESOperator();
	    				String resp = new String(messageInfo.getResponse());
	                    String body = resp.substring(bodyOffset);
	                    String deBody= aes.decrypt(body);
	                    deBody = deBody.replace("\"", "\\\"");
	                    String UnicodeBody = (new CUnicode()).unicodeDecode(deBody);
	                    //String newBody = body +"\r\n" +UnicodeBody; //将新的解密后的body附到旧的body后面
	                    String newBody = UnicodeBody;
	                    byte[] bodybyte = newBody.getBytes();
	                    //更新包的方法二buildHttpMessage
	                    messageInfo.setResponse(helpers.buildHttpMessage(header, bodybyte));
    				}catch(Exception e){
    					stdout.println(e);
    				}
    			}
    			
    		}	    		
    	}
    		
    }
}