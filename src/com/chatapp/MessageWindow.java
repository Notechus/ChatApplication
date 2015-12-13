package com.chatapp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import com.chatapp.networking.Packet;

public class MessageWindow extends JFrame
{

	/** Default UID because this class is serializable */
	private static final long serialVersionUID = 1L;
	/** UDP client reference */
	private ClientWindow window_ref;
	/** GUI running thread */
	private Thread run;
	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextArea txtrHistory;
	private DefaultCaret caret;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	private JMenu mnHelp;

	/**
	 * Create the frame.
	 */
	public MessageWindow(ClientWindow parent, String name)
	{

		window_ref = parent;
		setTitle(name);
		createWindow();
		console("You have opened chat with " + name);
	}

	/**
	 * Launch the application.
	 */
	public void createWindow()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 400);
		// setMinimumSize(new Dimension(600, 400));
		setResizable(false);

		JMenuBar menuBar_1 = new JMenuBar();
		setJMenuBar(menuBar_1);

		JMenu mnFile_1 = new JMenu("File");
		menuBar_1.add(mnFile_1);

		JMenu mnOptions = new JMenu("Options");
		menuBar_1.add(mnOptions);

		JMenu mnHelp_1 = new JMenu("Help");
		menuBar_1.add(mnHelp_1);
		getContentPane().setLayout(null);

		txtrHistory = new JTextArea();
		txtrHistory.setBounds(12, 12, 465, 290);
		getContentPane().add(txtrHistory);

		txtMessage = new JTextField();
		txtMessage.setBounds(12, 314, 465, 25);
		getContentPane().add(txtMessage);
		txtMessage.setColumns(10);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (window_ref.getClient().connected)
				{
					window_ref.send(Packet.Type.DIRECT_MESSAGE, txtMessage.getText());
				}
			}
		});
		btnSend.setBounds(508, 314, 80, 25);
		getContentPane().add(btnSend);

		setVisible(true);
	}

	/**
	 * Prints to console
	 * 
	 * @param message message to be printed
	 */
	public void console(String message)
	{
		txtrHistory.append(message + "\n\r");
	}
}
