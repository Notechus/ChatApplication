package com.chatapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client
{
	private DatagramSocket socket;
	private String name, address;
	private int port;
	private int ID = -1;
	public boolean connected = false;

	private InetAddress ip;
	private Thread send;

	public Client(String name_, String address_, int port_)
	{
		this.name = name_;
		this.address = address_;
		this.port = port_;
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

	public boolean openConnection(String address_)
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

	public String receive()
	{
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);

		try
		{
			socket.receive(packet);
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
		String message = new String(packet.getData()).trim();
		return message;
	}

	public void send(final byte[] data)
	{
		send = new Thread("Send")
		{
			public void run()
			{
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try
				{
					socket.send(packet);
				} catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		};
		send.start();
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
				}
			}
		}.start();
	}
}
