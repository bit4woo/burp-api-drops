package burp;

import java.util.ArrayList;
import java.util.List;

//from https://github.com/PortSwigger/example-custom-scan-insertion-points/blob/master/java/BurpExtender.java
//!!!要使用这个文件中的代码，需要先将文件名改为BurpExtender.java
public class BurpExtender implements IBurpExtender, IScannerInsertionPointProvider
{
    private IExtensionHelpers helpers;
    
    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)
    {
        helpers = callbacks.getHelpers();
        callbacks.setExtensionName("Serialized input scan insertion point");
        callbacks.registerScannerInsertionPointProvider(this);
    }

    //
    // implement IScannerInsertionPointProvider
    //
    
    @Override
    public List<IScannerInsertionPoint> getInsertionPoints(IHttpRequestResponse baseRequestResponse)
    {
        // retrieve the data parameter
        IParameter dataParameter = helpers.getRequestParameter(baseRequestResponse.getRequest(), "data");
        if (dataParameter == null)
            return null;
        
        // if the parameter is present, add a single custom insertion point for it
        List<IScannerInsertionPoint> insertionPoints = new ArrayList<IScannerInsertionPoint>();
        insertionPoints.add(new InsertionPoint(baseRequestResponse.getRequest(), dataParameter.getValue()));
        return insertionPoints;
    }

    //
    // class implementing IScannerInsertionPoint
    //
    private class InsertionPoint implements IScannerInsertionPoint
    {
        private byte[] baseRequest;
        private String insertionPointPrefix;
        private String baseValue;
        private String insertionPointSuffix;

        InsertionPoint(byte[] baseRequest, String dataParameter)
        {
            this.baseRequest = baseRequest;
            
            // URL解码、base64解码后的字符串，类似这样 input=1111&time=1628326168221
            dataParameter = helpers.bytesToString(helpers.base64Decode(helpers.urlDecode(dataParameter)));

            // 解析input字符串的位置 
            int start = dataParameter.indexOf("input=") + 6;
            insertionPointPrefix = dataParameter.substring(0, start);
            int end = dataParameter.indexOf("&", start);
            if (end == -1)
                end = dataParameter.length();
            baseValue = dataParameter.substring(start, end);
            insertionPointSuffix = dataParameter.substring(end, dataParameter.length());
        }

        // 
        // implement IScannerInsertionPoint
        //
        
        @Override
        public String getInsertionPointName()
        {
            return "Base64-wrapped input";
        }

        @Override
        public String getBaseValue()
        {
            return baseValue;
        }

        @Override
        public byte[] buildRequest(byte[] payload)
        {
            // 构造扫描时插入payload的请求
            String input = insertionPointPrefix + helpers.bytesToString(payload) + insertionPointSuffix;
            
            // base64编码+URL编码
            input = helpers.urlEncode(helpers.base64Encode(input));
            
            // 用新的请求参数来更新请求数据包
            return helpers.updateParameter(baseRequest, helpers.buildParameter("data", input, IParameter.PARAM_BODY));
        }

        @Override
        public int[] getPayloadOffsets(byte[] payload)
        {
            // since the payload is being inserted into a serialized data structure, there aren't any offsets 
            // into the request where the payload literally appears
            return null;
        }

        @Override
        public byte getInsertionPointType()
        {
            return INS_EXTENSION_PROVIDED;
        }
    }
}