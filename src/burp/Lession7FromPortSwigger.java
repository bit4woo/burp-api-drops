package burp;

import java.util.Arrays;
import java.util.List;

//!!!要使用这个文件中的代码，需要先将文件名改为BurpExtender.java
//来自 https://github.com/PortSwigger/example-custom-session-tokens/blob/master/java/BurpExtender.java
//只是添加了中文备注
public class BurpExtender implements IBurpExtender, ISessionHandlingAction
{
	private static final String SESSION_ID_KEY = "X-Custom-Session-Id:";
	private static final byte[] SESSION_ID_KEY_BYTES = SESSION_ID_KEY.getBytes();
	private static final byte[] NEWLINE_BYTES = new byte[] { '\r', '\n' };

	private IExtensionHelpers helpers;

	// 实现 IBurpExtender类
	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
	{
		// 保存helpers供后续使用
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

		//提取响应包的headers
		final byte[] finalResponse = macroItems[macroItems.length - 1].getResponse();
		if (finalResponse == null) return;

		final List<String> headers = helpers.analyzeResponse(finalResponse).getHeaders();

		String sessionToken = null;
		for (String header : headers)
		{
			// 跳过非"X-Custom-Session-Id"开头的header
			if (!header.startsWith(SESSION_ID_KEY)) continue;

			// 获取token值
			sessionToken = header.substring((SESSION_ID_KEY).length()).trim();
		}

		// 如果失败就返回
		if (sessionToken == null) return;

		final byte[] req = currentRequest.getRequest();
		
		//获取token key的开始位置和结束位置
		final int sessionTokenKeyStart = helpers.indexOf(req, SESSION_ID_KEY_BYTES, false, 0, req.length);
		final int sessionTokenKeyEnd = helpers.indexOf(req, NEWLINE_BYTES, false, sessionTokenKeyStart, req.length);

		// 组合新的请求包，实现token值的替换
		currentRequest.setRequest(join(
				Arrays.copyOfRange(req, 0, sessionTokenKeyStart),
				helpers.stringToBytes(String.format("%s %s", SESSION_ID_KEY, sessionToken)),
				Arrays.copyOfRange(req, sessionTokenKeyEnd, req.length)
				));
	}

	//拼接多个byte[]数组的方法
	private static byte[] join(byte[]... arrays)
	{
		int len = 0;
		for (byte[] arr : arrays)
		{
			len += arr.length;
		}

		byte[] result = new byte[len];
		int idx = 0;

		for (byte[] arr : arrays)
		{
			for (byte b : arr)
			{
				result[idx++] = b;
			}
		}

		return result;
	}
}
