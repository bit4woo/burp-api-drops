package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;


public class BurpExtender2 implements IBurpExtender, IHttpListener,IContextMenuFactory
{
    private IExtensionHelpers helpers;
	private IBurpExtenderCallbacks callbacks;
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
    	this.helpers = callbacks.getHelpers();
		this.callbacks = callbacks;
    	callbacks.printOutput("Para Encrypter For sfsy");
        callbacks.setExtensionName("Para Encrypter For sfsy");
        callbacks.registerHttpListener(this); 
        callbacks.registerContextMenuFactory(this);
    }

    @Override
    public void processHttpMessage(int toolFlag,boolean messageIsRequest,IHttpRequestResponse messageInfo)
    {
		
    	if (toolFlag == (toolFlag&configEnabledFor())){
			String Host = getHost(messageInfo);
			if (configScope().contains(Host)){//范围判断
	    		if (messageIsRequest){ //对请求包进行处理
    				try {
    					String body = new String( getBody(messageIsRequest,messageInfo));
    					String encryptedValue = URLEncoder.encode(Encryption.encryCode(body));

    					
    					callbacks.printOutput(body+":::::"+encryptedValue);

    					
	    				byte[] bodyByte = encryptedValue.getBytes();
	    				byte[] new_Request = helpers.buildHttpMessage(getHeaders(messageIsRequest,messageInfo), bodyByte);
	    				messageInfo.setRequest(new_Request);
	    				
					} catch (Exception e) {
						callbacks.printError(e.getMessage());
					}
	    		}else{
	    			try{
    					String body = new String(getBody(messageIsRequest,messageInfo));
    					String txtPlain = Encryption.decryCode(URLDecoder.decode(body));
    					
    					callbacks.printOutput("origin response:");
    					callbacks.printOutput(body);
    					callbacks.printOutput("decrypted response:");
    					callbacks.printOutput(txtPlain);
    					
	                    byte[] bodybyte = txtPlain.getBytes();
	                    messageInfo.setResponse(helpers.buildHttpMessage(getHeaders(messageIsRequest,messageInfo), bodybyte));
	    				}catch(Exception e){
	    					callbacks.printError(e.getMessage());
	    			}
	    		}
			}
    	}
    }
    
    
    @Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation)
	{ //需要在签名注册！！callbacks.registerContextMenuFactory(this);
	    List<JMenuItem> list = new ArrayList<JMenuItem>();
    	if(true) {//invocation.getToolFlag() == 16
    		
    		JMenuItem menuItem2 = new JMenuItem("xxx decrypto");
    		menuItem2.addActionListener(new decrypto(invocation));
    		list.add(menuItem2);
    		
        }
    	return list;
	}
    
    
	public class decrypto implements ActionListener{
		private IContextMenuInvocation invocation;

		public decrypto(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		
		
		@Override
		public void actionPerformed(ActionEvent event) {
			try {
				IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
				byte selectedInvocationContext = invocation.getInvocationContext();
				
				IHttpRequestResponse messageInfo = selectedItems[0];
		        	
	        	byte[] body= getBody(true,messageInfo);
	        	String clear = Encryption.decryCode(URLDecoder.decode(new String(body)));
	        	callbacks.printOutput("origin request");
	        	callbacks.printOutput(new String(body));
	        	callbacks.printOutput("decrypted request");
	        	callbacks.printOutput(clear);
	        	
	        	byte[] new_body = clear.getBytes();
	        	
	        	byte[] newRequestBytes = helpers.buildHttpMessage(getHeaders(true,messageInfo), new_body);
				
				if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
					selectedItems[0].setRequest(newRequestBytes);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				callbacks.printError(e.getMessage());
			}
		}
	}
    
    
    
    
    
    
	
	public  List<String> getHeaders(boolean messageIsRequest,IHttpRequestResponse messageInfo) {
		if(messageIsRequest) {
			IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
			List<String> headers = analyzeRequest.getHeaders();
			return headers;
		}else {
			IResponseInfo analyzeResponse = helpers.analyzeResponse(messageInfo.getResponse());
			List<String> headers = analyzeResponse.getHeaders();
			return headers;
		}
	}
	
	public byte[] getBody(boolean messageIsRequest,IHttpRequestResponse messageInfo) {
		if(messageIsRequest) {
			IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
			int bodyOffset = analyzeRequest.getBodyOffset();
			byte[] byte_Request = messageInfo.getRequest();
			
			byte[] byte_body = Arrays.copyOfRange(byte_Request, bodyOffset, byte_Request.length);//not length-1
			//String body = new String(byte_body); //byte[] to String
			return byte_body;
		}else {
			IResponseInfo analyzeResponse = helpers.analyzeResponse(messageInfo.getResponse());
			int bodyOffset = analyzeResponse.getBodyOffset();
			byte[] byte_Request = messageInfo.getResponse();
			
			byte[] byte_body = Arrays.copyOfRange(byte_Request, bodyOffset, byte_Request.length);//not length-1
			return byte_body;
		}
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
		List<String> scopes = new ArrayList<>();
		scopes.add("xxx.com");
		scopes.add("yyy.com");
		return scopes;
	}

}