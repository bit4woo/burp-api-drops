package burp;

//!!!要使用这个文件中的代码，需要先将文件名改为BurpExtender.java
public class BurpExtender implements IBurpExtender, ISessionHandlingAction
{
	private static final String SESSION_ID_KEY = "X-Custom-Session-Id";

	private IExtensionHelpers helpers;

	// 实现 IBurpExtender类
	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
	{
		// 保持helpers供后续使用
		this.helpers = callbacks.getHelpers();

		// 设置插件名称
		callbacks.setExtensionName("Session token example");
		callbacks.registerSessionHandlingAction(this);
	}

	// 实现 ISessionHandlingAction
	@Override
	public String getActionName()
	{
		return "Use session token from macro";
	}

	@Override
	public void performAction(
			IHttpRequestResponse currentRequest,
			IHttpRequestResponse[] macroItems)
	{
		if (macroItems.length == 0) return;

		// 从宏的配置的请求当中获取response
		final byte[] finalResponse = macroItems[macroItems.length - 1].getResponse();
		if (finalResponse == null) return;
		
		HelperPlus hp = new HelperPlus(helpers);
		//获取我们想要的session id
		String sessionToken = hp.getHeaderValueOf(false, finalResponse, SESSION_ID_KEY);

		// 如果获取失败，就返回
		if (sessionToken == null) return;

		final byte[] req = currentRequest.getRequest();
		//用获取到的session id修改当前数据包。
		byte[] newReq = hp.addOrUpdateHeader(true, req, SESSION_ID_KEY, sessionToken);
		currentRequest.setRequest(newReq);
	}
}
