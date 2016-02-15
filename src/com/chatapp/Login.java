package com.chatapp;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.chatapp.networking.UDPSocket;

/**
 * Login window class
 * 
 * @author notechus
 *
 */
public class Login extends JFrame implements GUIWindow
{
	/** Default UID because this class is serializable */
	private static final long serialVersionUID = 1L;
	/** GUI stuff */
	private JPanel contentPane;
	private JTextField txtName;
	private JLabel lblPassword;
	private JPasswordField pwdPassword;

	private InetAddress ip;
	private UDPSocket socket;

	// private final String address = "localhost";
	private final String address = "localhost";
	private final int port = 8192;
	private String username;

	private boolean connected = false;

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

		try
		{
			socket = new UDPSocket(this, -1, address, port);
			connected = socket.openConnection();
		} catch (UnknownHostException ex)
		{
			ex.printStackTrace();
		}
		// illegal argument ex tylko tymczasowo-coś trzeba było dać
		if (!connected) throw new IllegalArgumentException("Socket not connected");

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
					JOptionPane.showMessageDialog(Login.this, "Incorrect username or password.");
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
		new ClientWindow(socket, name, address, port);
	}

	private boolean authenticate() throws LoginException
	{
		boolean validated = true;
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

	@Override
	public void removeUser(User user)
	{
		// should do nothing here
	}

	@Override
	public void addUser(User user)
	{
		// should do nothing here
	}

	@Override
	public void console(String message)
	{
		// should do nothing here
	}
}
