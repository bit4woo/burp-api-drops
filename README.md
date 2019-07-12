# burp api drops

主要参考：

官方各种示例代码：https://portswigger.net/burp/extender

官方API文档：https://portswigger.net/burp/extender/api/



这个项目主要收集了个人写burp插件时用到的主要的方法和代码。



我们的主线任务就是**<u>处理请HTTP请求和响应</u>**。



### 起步--必须的方法和实现

```java
//所有burp插件都必须实现IBurpExtender接口，而且实现的类必须叫做BurpExtender
public class BurpExtender implements IBurpExtender，IHttpListener
{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    
    private PrintWriter stdout;
    private String ExtenderName = "bit4 extender name";
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {//IBurpExtender，必须实现的方法
    	stdout = new PrintWriter(callbacks.getStdout(), true);
    	stdout.println(ExtenderName);
        this.callbacks = callbacks;
        helpers = callbacks.getHelpers();
        callbacks.setExtensionName(ExtenderName); //插件名称
        callbacks.registerHttpListener(this); //如果没有注册，下面的processHttpMessage方法是不会生效的。处理请求和响应包的插件，这个应该是必要的
    }
```



### 处理http请求

1. 获取：request中的header，body，paramaters

```java
IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo); //对消息体进行解析


//**************获取参数 通过IRequestInfo对象*************************//
List<IParameter> paraList = analyzeRequest.getParameters();
//当body是json格式的时候，这个方法也可以正常获取到键值对；
//但是PARAM_JSON等格式不能通过updateParameter方法来更新。
//如果在url中的参数的值是 xxx=json格式的字符串 这种形式的时候，getParameters应该是无法获取到最底层的键值对的。
for (IParameter para : paraList){
    byte type= para.getType(); //获取参数的类型
    String key = para.getName(); //获取参数的名称
    String value = para.getValue(); //获取参数的值
}
//参数共有7种格式，0是URL参数，1是body参数，2是cookie参数，6是json格式参数



//**************获取headers 通过IRequestInfo对象*************************//
List<String> headers = analyzeRequest.getHeaders();


//**************获取body 通过String格式的request截取*************************//
int bodyOffset = analyzeRequest.getBodyOffset();

byte[] byte_Request = messageInfo.getRequest();//当需要byte[]和string格式的请求包时用这个方法！
String request = new String(byte_Request); //byte[] to String

String body = request.substring(bodyOffset);
byte[] byte_body = body.getBytes();  //String to byte[]

```

2. 修改：

```
修改实质就是对byte[]、String 变量的操作。毋庸赘述。
```



2. 更新：

```java
//***********************方法一：更新参数**********************
//参数共有7种格式，0是URL参数，1是body参数，2是cookie参数 （helpers.updateParameter只支持这三种）

IParameter newPara = helpers.buildParameter(key, Value, para.getType()); //构造新的参数
new_Request = helpers.updateParameter(new_Request, newPara); //构造新的请求包；关键方法
//不支持更新Json格式的参数，要注意！！！
messageInfo.setRequest(new_Request);//设置最终新的请求包


//***********************方法二：更新header或body********************************
//如果修改了header或者数修改了body，不能通过updateParameter，使用这个方法。
//6是json参数--这种参数的更新用更新body的方法。

List<String> headers = analyzeRequest.getHeaders();
byte[] bodyByte = newBody.getBytes();
byte[] new_Request = helpers.buildHttpMessage(headers, bodyByte); //关键方法
messageInfo.setRequest(new_Request);//设置最终新的请求包
```



### 处理HTTP响应

```java
//**************************获取***********************************
IResponseInfo analyzedResponse = helpers.analyzeResponse(messageInfo.getResponse()); //getResponse获得的是字节序列byte[]
short statusCode = analyzedResponse.getStatusCode();
List<String> headers = analyzedResponse.getHeaders();
String resp = new String(messageInfo.getResponse());
int bodyOffset = analyzedResponse.getBodyOffset();//响应包是没有参数的概念的，大多需要修改的内容都在body中
String body = resp.substring(bodyOffset);


//***************************更新********************************
byte[] bodybyte = newBody.getBytes();
messageInfo.setResponse(helpers.buildHttpMessage(headers, bodybyte));
```



### 扫描插件中更新请求的方法

```java

String paraName = insertionPoint.getInsertionPointName(); //实在就是参数名
String paraValue = insertionPoint.getBaseValue();//实质就是原始值
byte[] modifiedRawRequest = insertionPoint.buildRequest(payload.getBytes());//用payload的值【替换】原始的值；
//insertionPoint可以是参数值、整个body、新增的URL参数、新增的body参数；但没有定义【参数名】的注入点，如果想要用payload替换参数名，就需要自己实现了（通过更改参数的方式），

long sendTime = System.currentTimeMillis();
IHttpRequestResponse checkRequestResponse = callbacks.makeHttpRequest(
baseRequestResponse.getHttpService(), modifiedRawRequest);
```



### 错误输出方式

```java
private PrintWriter stdout;
private PrintWriter stderr;

stdout = new PrintWriter(callbacks.getStdout(), true);
stderr = new PrintWriter(callbacks.getStderr(), true);

try{
    xxxx;
}catch(Exception e){
    e.printStackTrace(stderr);
    //这种方式会输出错误栈，利于错误排查
}
```



### 常用HTTP信息获取方法

```java
package burp;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Getter {
    private static IExtensionHelpers helpers;
    public Getter(IExtensionHelpers helpers) {
    	this.helpers = helpers;
    }
    
	public List<String> getHeaders(boolean messageIsRequest,IHttpRequestResponse messageInfo) {
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
	
	public URL getURL(IHttpRequestResponse messageInfo){
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		return analyzeRequest.getUrl();
	}
	
	public String getHost(IHttpRequestResponse messageInfo) {
		return messageInfo.getHttpService().getHost();
	}
	
	public short getStatusCode(IHttpRequestResponse messageInfo) {
		IResponseInfo analyzedResponse = helpers.analyzeResponse(messageInfo.getResponse());
		return analyzedResponse.getStatusCode();
	}
	public List<IParameter> getParas(IHttpRequestResponse messageInfo){
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		return analyzeRequest.getParameters();
	}
}

```





### 一段完整的示例代码

```java
package burp;

import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.net.URLEncoder;
import custom.Unicode;


public class BurpExtender implements IBurpExtender, IHttpListener
{//所有burp插件都必须实现IBurpExtender接口，而且实现的类必须叫做BurpExtender
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    
    private PrintWriter stdout;
    private String ExtenderName = "burp extender api drops by bit4";
    private List<String> paraWhiteList = new ArrayList<String>();
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {//IBurpExtender，必须实现的方法
    	stdout = new PrintWriter(callbacks.getStdout(), true);
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
```







### 一些小的点

1. buildRequest是在原始值后追加，还是替换原始值？

		byte[] requsetbyte = insertionPoint.buildRequest((insertionPoint.getBaseValue()+payload).getBytes());
		//buildRequest是在原始值后追加，还是替换原始值？ 经测试是替换！！！
2. burp插件开发中，子类异常不要使用system.exit(0)，否则整个burp都将退出！



3. 添加issue时有2种方法（还是强烈建议使用issues.add的方法！！！避免漏报！）

   ```
   1、 通过callbacks.addScanIssue ，这种方法添加的issue，中不会包含当前插入点。适合用于url级别、host级别的扫描。
       burp官网不建议使用的方法，通过它添加的漏洞，不会在scanqueue中有记录！！！【在实际的debug测试中还出现过明明调用成功，却发现连target中都没有记录的情况！！！】
   2、 通过创建List<IScanIssue> issues = new ArrayList<>(); 然后在函数结尾返回这个list，然后会由burp进行issue的添加，这个适合burp是知道当前插入点的，会包含插入点名称。    这是burp建议的添加漏洞的方法，实际测试它也是有优势的！通过它上报的漏洞会在【scanqueue和 target】中都有记录。而且这个方法的整体逻辑中有自动判断去重的能力！唯一的缺点就是始终包含插入点信息！
   
   3、值得注意的是，在我自己修改的J2EEScan中，由于通过数据组
   
   ```

   