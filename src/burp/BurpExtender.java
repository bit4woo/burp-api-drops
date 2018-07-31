package burp;

import java.util.ArrayList;
import java.util.List;

import burp.IBurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpListener;
import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.IRequestInfo;
import burp.IResponseInfo;

import java.io.PrintWriter;
import java.net.URLEncoder;
import custom.Unicode;


public class BurpExtender implements IBurpExtender, IHttpListener
{//所有burp插件都必须实现IBurpExtender接口，而且实现的类必须叫做BurpExtender
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    
    private PrintWriter stdout;
    private PrintWriter stderr;
    private String ExtenderName = "burp extender api drops by bit4";
    private List<String> paraWhiteList = new ArrayList<String>();
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {//IBurpExtender，必须实现的方法
    	stdout = new PrintWriter(callbacks.getStdout(), true);
    	stderr = new PrintWriter(callbacks.getStderr(), true);
    	callbacks.printOutput(ExtenderName);
    	//stdout.println(ExtenderName);
        this.callbacks = callbacks;
        helpers = callbacks.getHelpers();
        callbacks.setExtensionName(ExtenderName); 
        callbacks.registerHttpListener(this); //如果没有注册，下面的processHttpMessage方法是不会生效的。处理请求和响应包的插件，这个应该是必要的
    }

    @Override
    public void processHttpMessage(int toolFlag,boolean messageIsRequest,IHttpRequestResponse messageInfo)
    {
    	/*
    	try{
    		Class.forName("burp.j2ee.issues.collaimpl."+"xxx").getConstructor();
    	}catch(Exception e){
    	    e.printStackTrace(stderr);
    	    callbacks.printError("------1---------\n");
    	    callbacks.printError(e.toString());
    	    callbacks.printError("-------2--------\n");
    	    callbacks.printError(e.getMessage());
    	    callbacks.printError("-------3--------\n");
    	    callbacks.printError(e.getStackTrace().toString());
    	    callbacks.printError("-------4--------\n");
    	}
    	*/
    	if (toolFlag == (toolFlag&checkEnabledFor(callbacks))){
    		//不同的toolFlag代表了不同的burp组件 https://portswigger.net/burp/extender/api/constant-values.html#burp.IBurpExtenderCallbacks
        	example(messageIsRequest, messageInfo);
    	}
    }
    
    public void example(boolean messageIsRequest,IHttpRequestResponse messageInfo){
		if (messageIsRequest){ //对请求包进行处理
			
			int flag = 3;//当我们需要修改一个HTTP request，一般会涉及3部分：parameters\body\headers
			IRequestInfo analyzeRequest;
			
			switch (flag) {
			case 1://修改参数部分 parameters
			{
				analyzeRequest = helpers.analyzeRequest(messageInfo); 
				//对消息体进行解析,messageInfo是整个HTTP请求和响应消息体的总和，各种HTTP相关信息的获取都来自于它，HTTP流量的修改都是围绕它进行的。
    			
    			List<IParameter> paraList = analyzeRequest.getParameters();
    			//the method of get parameter
    			//当body是json格式的时候，这个方法也可以正常获取到键值对；但是PARAM_JSON等格式不能通过updateParameter方法来更新。
                //如果在url中的参数的值是 key=json格式的字符串 这种形式的时候，getParameters应该是无法获取到最底层的键值对的。
    			
    			byte[] new_Request = messageInfo.getRequest();
    			
    			
    			for (IParameter para : paraList){// 循环获取参数，判断类型，进行加密处理后，再构造新的参数，合并到新的请求包中。
    				if ((para.getType() == 0 || para.getType() == 1) && !paraWhiteList.contains(para.getName())){ 
    					//getTpe()就是来判断参数是在那个位置的，cookie中的参数是不需要进行加密处理的。还要排除白名单中的参数。
	    				//参数共有7种格式，0是URL参数，1是body参数，2是cookie参数 （helpers.updateParameter只支持这三种）
    					//6是json参数--这种参数的更新用更新body的方法。
    					String key = para.getName(); //获取参数的名称
	    				String value = para.getValue(); //获取参数的值
	    				//stdout.println(key+":"+value);
	    				String changedValue;
						try {
							changedValue = value+"*********************";
							changedValue = URLEncoder.encode(changedValue); //还要进行URL编码，否则会出现= 等特殊字符导致参数判断异常;
		    				//stdout.println(key+":"+value+":"+encryptedValue); //输出到extender的UI窗口，可以让使用者有一些判断
							
		    				IParameter newPara = helpers.buildParameter(key, changedValue, para.getType()); //构造新的参数
		    				new_Request = helpers.updateParameter(new_Request, newPara); //构造新的请求包
		    				messageInfo.setRequest(new_Request);//设置最终新的请求包
		    				
						} catch (Exception e) {
							
							callbacks.printError(e.getMessage());
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
					String newBody = body+"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$";
					stdout.println(newBody);
					byte[] bodyByte = newBody.getBytes();
	    			byte[] new_Request = helpers.buildHttpMessage(headers, bodyByte); 
	    			//如果修改了header或者数修改了body，不能通过updateParameter，使用这个方法。
	    			messageInfo.setRequest(new_Request);//设置最终新的请求包
				} catch (Exception e) {
					callbacks.printError(e.getMessage());
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
			List<String> headers = analyzedResponse.getHeaders();
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
	                    String deBody= body+"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&";
	                    deBody = deBody.replace("\"", "\\\"");
	                    String UnicodeBody = (new Unicode()).unicodeDecode(deBody);
	                    String newBody = body +"\r\n" +UnicodeBody; //将新的解密后的body附到旧的body后面
	                    //String newBody = UnicodeBody;
	                    byte[] bodybyte = newBody.getBytes();
	                    messageInfo.setResponse(helpers.buildHttpMessage(headers, bodybyte));
    				}catch(Exception e){
    					callbacks.printError(e.getMessage());
    				}
    			}
				break;
			default:
				break;
			}
		}	    		
    }

    
	public int checkEnabledFor(IBurpExtenderCallbacks callbacks){
		//get values that should enable this extender for which Component.
		//不同的toolflag代表了不同的burp组件 https://portswigger.net/burp/extender/api/constant-values.html#burp.IBurpExtenderCallback
		return callbacks.TOOL_PROXY+callbacks.TOOL_REPEATER+callbacks.TOOL_SCANNER+callbacks.TOOL_INTRUDER;
	}
}