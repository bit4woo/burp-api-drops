package burp;

import java.awt.Component;
//!!!要使用这个文件中的代码，需要先将文件名改为BurpExtender.java
public class BurpExtender implements IBurpExtender, IMessageEditorTabFactory
{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;

    //
    // implement IBurpExtender
    // 实现IBurpExtender接口
    //

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        // keep a reference to our callbacks object
        this.callbacks = callbacks;

        // obtain an extension helpers object  获取helpers对象
        helpers = callbacks.getHelpers();

        // set our extension name 设置插件名称
        callbacks.setExtensionName("Serialized input editor");

        // register ourselves as a message editor tab factory
        // 把我们当前对象注册成为 “message editor tab”的工厂
        callbacks.registerMessageEditorTabFactory(this);
    }

    //
    // implement IMessageEditorTabFactory 实现IMessageEditorTabFactory接口
    // burp显示HTTP数据包是通过IMessageEditor，每次创建一个新的IMessageEditor时，都会调用这个函数。
    //

    @Override
    public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable)
    {
        // create a new instance of our custom editor tab
        // 创建一个新的实例，我们自定义的编辑tab的实例
        return new Base64InputTab(controller, editable);
    }

    //
    // class implementing IMessageEditorTab
    // 实现IMessageEditorTab类
    //

    class Base64InputTab implements IMessageEditorTab
    {
        private boolean editable;
        private ITextEditor txtInput;
        private byte[] currentMessage;

        public Base64InputTab(IMessageEditorController controller, boolean editable)
        {
            this.editable = editable;
            // create an instance of Burp's text editor, to display our deserialized data
            txtInput = callbacks.createTextEditor();
            txtInput.setEditable(editable);
        }

        //
        // implement IMessageEditorTab
        //

        @Override
        public String getTabCaption() //tab的名称
        {
            return "Serialized input";
        }

        @Override
        public Component getUiComponent()
        {
            return txtInput.getComponent();//组件
        }

        @Override
        public boolean isEnabled(byte[] content, boolean isRequest)//是否启用当前tab
        {
            // enable this tab for requests containing a data parameter
            // 是请求包，并且有一个data参数时启用
            return isRequest && null != helpers.getRequestParameter(content, "data");
        }

        @Override
        public void setMessage(byte[] content, boolean isRequest)
        {//控制文本框的显示内容。
            if (content == null)
            {
                // clear our display 清空显示内容
                txtInput.setText(null);
                txtInput.setEditable(false);
            }
            else
            {
                // retrieve the data parameter  获取data参数
                IParameter parameter = helpers.getRequestParameter(content, "data");

                // deserialize the parameter value 反序列化参数的值，也就是base64解码
                txtInput.setText(helpers.base64Decode(helpers.urlDecode(parameter.getValue())));
                txtInput.setEditable(editable);
            }

            // remember the displayed content
            currentMessage = content;
        }

        @Override
        public byte[] getMessage()
        {
            // determine whether the user modified the deserialized data
            // 判断文本框中的内容是否被用户修改过了
            if (txtInput.isTextModified())
            {
                // reserialize the data 从新序列号，即base64编码。
                byte[] text = txtInput.getText();
                String input = helpers.urlEncode(helpers.base64Encode(text));

                // update the request with the new parameter value
                //根据用户的修改来更新当前的请求数据包。
                return helpers.updateParameter(currentMessage, helpers.buildParameter("data", input, IParameter.PARAM_BODY));
            }
            else return currentMessage;
        }

        @Override
        public boolean isModified()
        {
            return txtInput.isTextModified();
        }

        @Override
        public byte[] getSelectedData()
        {
            return txtInput.getSelectedText();
        }
    }
}