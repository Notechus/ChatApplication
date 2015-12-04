package com.chatapp;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.chatapp.security.authentication.UserAuthentication;

/**
 * Login window class
 * 
 * @author notechus
 *
 */
public class Login extends JFrame
{
	/** Default UID because this class is serializable */
	private static final long serialVersionUID = 1L;
	/** GUI stuff */
	private JPanel contentPane;
	private JTextField txtName;
	private JLabel lblPassword;
	private JPasswordField pwdPassword;

	private InetAddress ip;

	private final String address = "localhost";
	private final int port = 8192;
	private String username;

	private LoginContext lc;

	/**
	 * Create the frame.
	 */
	public Login()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtName = new JTextField();
		txtName.setHorizontalAlignment(SwingConstants.CENTER);
		txtName.setBounds(82, 65, 130, 25);
		contentPane.add(txtName);
		txtName.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(118, 45, 46, 14);
		contentPane.add(lblName);

		lblPassword = new JLabel("Password:");
		lblPassword.setBounds(118, 103, 89, 14);
		contentPane.add(lblPassword);

		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				username = txtName.getText();
				boolean validated = false;
				try
				{
					validated = authenticate();
					UserAuthentication au = new UserAuthentication(username, pwdPassword.getPassword());
				} catch (LoginException ex)
				{
					ex.printStackTrace();
				}
				if (validated)
				{
					login(username, address, port);
					System.out.println("Logged in");
				} else
				{

				}
			}
		});
		btnLogin.setBounds(102, 246, 89, 23);
		contentPane.add(btnLogin);

		pwdPassword = new JPasswordField();
		pwdPassword.setBounds(82, 124, 130, 25);
		contentPane.add(pwdPassword);
	}

	/**
	 * Login happens here
	 */
	private void login(String name, String address, int port)
	{
		dispose();
		new ClientWindow(name, address, port);
		// new ClientWindow(name, passwd, port);
		/*
		 * for (char i : passwd) { i = 0; // zeroes password in memory }
		 */
	}

	private boolean authenticate() throws LoginException
	{
		boolean validated = false;
		char[] passwd = pwdPassword.getPassword();

		return validated;
	}

	/**
	 * Main function of application
	 * 
	 * @param args
	 *            no use of arguments here
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
