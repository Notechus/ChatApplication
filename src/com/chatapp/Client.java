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

	public Client(ClientWindow parent, String name_, String address_, int port_)
	{
		this.name = name_;
		this.address = address_;
		this.port = port_;
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
		Packet p = null;
		try
		{
			socket.receive(packet);
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			p = (Packet) is.readObject();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		} catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
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
					Packet p = new Packet(ID, type, message);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(p);
					byte[] data = outputStream.toByteArray();
					DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
					socket.send(packet);
				} catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		};
		send.start();
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
						connected = false; // just in case(same as messages and send button)
						console("You have timed out\n");
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
