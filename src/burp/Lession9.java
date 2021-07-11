package burp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;

//!!!要使用这个文件中的代码，需要先将文件名改为BurpExtender.java
public class BurpExtender implements IBurpExtender, ITab,IContextMenuFactory
{
    private static IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    GUI gui;
    
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
        callbacks.setExtensionName("Custom GUI");
		gui = new GUI();
        // 添加tab
        callbacks.addSuiteTab(this);
        callbacks.registerContextMenuFactory(this);
    }

	@Override
	public String getTabCaption() {
		return "Custom GUI";
	}

	@Override
	public Component getUiComponent() {
		return gui.getComponent(0);
	}

	public static IBurpExtenderCallbacks getCallbacks() {
		return callbacks;
	}

	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		ArrayList<JMenuItem> menu_item_list = new ArrayList<JMenuItem>();

		JMenuItem sendto = new JMenuItem("sendtoGUI");
		sendto.addActionListener(new sendto(invocation));
		menu_item_list.add(sendto);
		
		return menu_item_list;
	}
	
	public class sendto implements ActionListener{
		private IContextMenuInvocation invocation;

		public sendto(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			if (messages != null && messages.length >0) {
				gui.setGUIMessage(messages[0]);
			}
			System.out.println(Arrays.toString(gui.getRequest()));
		}
	}
}


