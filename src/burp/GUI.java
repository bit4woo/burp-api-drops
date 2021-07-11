package burp;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/** 
* @author bit4woo
* @github https://github.com/bit4woo 
* @version CreateTime：2021年7月11日 下午5:22:02 
*/
//Lession9对应的源码
public class GUI extends JFrame implements IMessageEditorController {

	private JPanel contentPane;
	private IHttpRequestResponse GUIMessage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		//添加一个editor，用于显示HTTP数据包
		IMessageEditor editor = BurpExtender.getCallbacks().createMessageEditor(this, false);
		contentPane.add(editor.getComponent(), BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		
		JButton btnNewButton = new JButton("displayRequest");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.setMessage(getRequest(), true);//调用IMessageEditorController的函数来显示请求包
			}
		});
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("displayResponse");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.setMessage(getResponse(), true);//调用IMessageEditorController的函数来显示响应包
			}
		});
		panel.add(btnNewButton_1);
	}
	
	@Override //IMessageEditorController的函数
	public IHttpService getHttpService() {
		return GUIMessage.getHttpService();
	}

	@Override //IMessageEditorController的函数
	public byte[] getRequest() {
		return GUIMessage.getRequest();
	}

	@Override //IMessageEditorController的函数
	public byte[] getResponse() {
		return GUIMessage.getResponse();
	}

	public IHttpRequestResponse getGUIMessage() {
		return GUIMessage;
	}

	public void setGUIMessage(IHttpRequestResponse gUIMessage) {
		GUIMessage = gUIMessage;
	}
}
