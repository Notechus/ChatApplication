package com.chatapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import com.chatapp.networking.Packet;

public class Client
{
	private DatagramSocket socket;
	private String name, address;
	private int port;
	private int ID = -1;
	public boolean connected = false;
	private boolean running = false;

	private InetAddress ip;
	private Thread send, listen;
	private ClientWindow window_ref;

	// type of currently used cipher
	//private final String cipher_const = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	private final String cipher_const = "AES";
	// password for encrypting packets
	private final String encrypt_passwd = "Somerandompasswd"; // 16

	/*
	 * TODO: add encrypting to packets, add user-user communication(necessary to
	 * add new packet type for this), add acknowledgement packet, get separate
	 * udp for ping and dc, add logging system, add exceptions management
	 * improve gui, add sounds, add database, change login for users from db,
	 * add registration, add new uid system
	 */
	public Client(ClientWindow parent, String name_, String address_, int port_)
	{
		this.name = name_;
		this.address = address_;
		this.port = port_;
		window_ref = parent;
		running = true;
	}

	public String getName()
	{
		return name;
	}

	public String getAddress()
	{
		return address;
	}

	public int getPort()
	{
		return port;
	}

	public void setID(int id)
	{
		this.ID = id;
	}

	public int getID()
	{
		return ID;
	}

	public boolean openConnection()
	{
		try
		{
			socket = new DatagramSocket();
			ip = InetAddress.getByName(address);
		} catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			return false;
		} catch (SocketException ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public Packet receive()
	{
		byte[] data = new byte[65536];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		SealedObject d_packet = null;
		Packet p = null;
		try
		{
			socket.receive(packet);
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			d_packet = (SealedObject) is.readObject();
			p = decrypt(d_packet);
		} catch (IOException ex)
		{
			ex.printStackTrace();
		} catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}

		return p;
	}

	public void send(Packet.Type type, String message)
	{
		send = new Thread("Send")
		{
			public void run()
			{
				try
				{
					Packet p = new Packet(ID, type, message); // sends id
					SealedObject e_packet = encrypt(p);
					// make wrapper class for encrypted packet
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(e_packet);
					byte[] data = outputStream.toByteArray();
					DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
					socket.send(packet);
				} catch (IOException ex)
				{
					ex.printStackTrace();
				} catch (NoSuchAlgorithmException e)
				{
					e.printStackTrace();
				} catch (NoSuchPaddingException e)
				{
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	// TODO
	protected SealedObject encrypt(final Packet p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_const);
		Cipher c = Cipher.getInstance(cipher_const);
		SealedObject encrypted_message = null;
		try
		{
			c.init(Cipher.ENCRYPT_MODE, message);
			encrypted_message = new SealedObject(p, c);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return encrypted_message;
	}

	// TODO
	protected Packet decrypt(final SealedObject p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_const);
		Cipher c = Cipher.getInstance(cipher_const);
		Packet decrypted_message = null;
		try
		{
			c.init(Cipher.DECRYPT_MODE, message);
			decrypted_message = (Packet) p.getObject(c);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return decrypted_message;
	}

	public void listen()
	{
		listen = new Thread("Listen")
		{
			public void run()
			{
				while (running)
				{
					Packet packet = receive();
					if (packet.type == Packet.Type.CONNECT)
					{
						setID(Integer.parseInt(packet.message));
						connected = true;
						console("Succesfully connected to server with ID: " + getID());
					} else if (packet.type == Packet.Type.MESSAGE)
					{
						console(packet.message);
					} else if (packet.type == Packet.Type.PING)
					{
						send(packet.type, "ping");
					} else if (packet.type == Packet.Type.DISCONNECT)
					{
						console("You have timed out");
						connected = false;

					}
				}
			}
		};
		listen.start();
	}

	public void console(String message)
	{
		window_ref.console(message);
	}

	public void close()
	{
		new Thread()
		{
			public void run()
			{
				synchronized (socket)
				{
					socket.close();
					running = false;
				}
			}
		}.start();
	}
}
