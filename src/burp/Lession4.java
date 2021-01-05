package burp;

import java.io.PrintWriter;

//!!!要使用这个文件中的代码，需要先将文件名改为BurpExtender.java

public class BurpExtender implements IBurpExtender,IHttpListener,IProxyListener,IScannerListener,IExtensionStateListener
{
	PrintWriter stdout;
	PrintWriter stderr;
	IBurpExtenderCallbacks callbacks;
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
    	this.callbacks = callbacks;
        // 设置插件的名称
        callbacks.setExtensionName("Hello world extension");
        
        // 获取burp提供的标准输出流和错误输出流
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(), true);
        
        // 打印到标准输出流
        stdout.println("Hello output");
        
        // 答应到错误输出流
        stderr.println("Hello errors");
        
        // 写一个报警信息到burp的报警面板
        callbacks.issueAlert("Hello alerts");
        
        // 抛出一个异常，将会在错误输出流中显示
        throw new RuntimeException("Hello exceptions");
    }

    //
    // implement IHttpListener
    //

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
        stdout.println(
                (messageIsRequest ? "HTTP request to " : "HTTP response from ") +
                messageInfo.getHttpService() +
                " [" + callbacks.getToolName(toolFlag) + "]");
    }

    //
    // implement IProxyListener
    //

    @Override
    public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message)
    {
        stdout.println(
                (messageIsRequest ? "Proxy request to " : "Proxy response from ") +
                message.getMessageInfo().getHttpService());
    }

    //
    // implement IScannerListener
    //

    @Override
    public void newScanIssue(IScanIssue issue)
    {
        stdout.println("New scan issue: " + issue.getIssueName());
    }

    //
    // implement IExtensionStateListener
    //

    @Override
    public void extensionUnloaded()
    {
        stdout.println("Extension was unloaded");
    }
}