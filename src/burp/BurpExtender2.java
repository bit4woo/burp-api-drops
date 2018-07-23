package burp;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import com.sf.encryption.Encryption;


public class BurpExtender implements IBurpExtender, IHttpListener
{
    private IExtensionHelpers helpers;
	private IBurpExtenderCallbacks callbacks;
    
    // implement IBurpExtender
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
    	this.helpers = callbacks.getHelpers();
		this.callbacks = callbacks;
    	callbacks.printOutput("v2");
        callbacks.setExtensionName("v2"); //插件名称
        callbacks.registerHttpListener(this); //如果没有注册，下面的processHttpMessage方法是不会生效的。处理请求和响应包的插件，这个应该是必要的
    }

    @Override
    public void processHttpMessage(int toolFlag,boolean messageIsRequest,IHttpRequestResponse messageInfo)
    {
		
    	if (toolFlag == (toolFlag&configEnabledFor())){ //不同的toolflag代表了不同的burp组件 https://portswigger.net/burp/extender/api/constant-values.html#burp.IBurpExtenderCallbacks
    		if (messageIsRequest){ //对请求包进行处理
    			String Host = messageInfo.getHttpService().toString();
    			if (configScope().contains(Host)){//范围判断

    				try {
    					String body = new String( getBody(messageInfo));
    					String encryptedValue = URLEncoder.encode(Encryption.encryCode(body));

    					
    					callbacks.printOutput(body+":"+encryptedValue);

    					
	    				byte[] bodyByte = encryptedValue.getBytes();
	    				byte[] new_Request = helpers.buildHttpMessage(getHeaders(messageInfo), bodyByte);
	    				messageInfo.setRequest(new_Request);
	    				
					} catch (Exception e) {
						callbacks.printError(e.getMessage());
					}
    			}
    		}
    		
    		else{
    			try{
    					String body = new String(getBody(messageInfo));
    					String txtPlain = Encryption.decryCode(URLDecoder.decode(body));
    					
	                    byte[] bodybyte = txtPlain.getBytes();
	                    messageInfo.setResponse(helpers.buildHttpMessage(getHeaders(messageInfo), bodybyte));
    				}catch(Exception e){
    					callbacks.printError(e.getMessage());
    				}
    			}   		
    	}
    }
	
	public  List<String> getHeaders(IHttpRequestResponse messageInfo) {
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		List<String> headers = analyzeRequest.getHeaders();
		return headers;
	}
	
	public byte[] getBody(IHttpRequestResponse messageInfo) {
			IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
			int bodyOffset = analyzeRequest.getBodyOffset();
			byte[] byte_Request = messageInfo.getRequest();
			
			byte[] byte_body = Arrays.copyOfRange(byte_Request, bodyOffset, byte_Request.length-1);
			//String body = new String(byte_body); //byte[] to String
			return byte_body;
	}
	
	public String getShortUrl(IHttpRequestResponse messageInfo) {
		return messageInfo.getHttpService().toString();
	}
	
	public String getHost(IHttpRequestResponse messageInfo) {
		return messageInfo.getHttpService().getHost();
	}
	
	public List<IParameter> getParas(IHttpRequestResponse messageInfo){
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		return analyzeRequest.getParameters();
	}
	
	
	
	public int configEnabledFor() {
		int status = IBurpExtenderCallbacks.TOOL_INTRUDER+
					IBurpExtenderCallbacks.TOOL_REPEATER+
					IBurpExtenderCallbacks.TOOL_SCANNER;
		return status;
	}
	public List<String> configScope(){
		List<String> scopes = null;
		scopes.add("");
		return scopes;
	}

}