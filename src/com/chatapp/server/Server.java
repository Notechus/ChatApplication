package com.chatapp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable
{
	private List<ServerClient> clients = new ArrayList<>();
	private List<Integer> clientResponse = new ArrayList<>();
	// security bug with sending actual codes with messages eg user writes /dc/81223
	// and disconnects some other user-( not existing in here)
	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;

	private final int MAX_ATTEMPTS = 5;

	public Server(int port_)
	{
		this.port = port_;
		try
		{
			socket = new DatagramSocket(port);
		} catch (SocketException ex)
		{
			ex.printStackTrace();
			return;
		}

		run = new Thread(this, "Server");
		run.start();
	}

	public void run()
	{
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
	}

	private void manageClients()
	{
		manage = new Thread("Manage")
		{
			public void run()
			{
				while (running)
				{
					sendToAll("/p/server");
					try
					{
						Thread.sleep(2000); // sleep to wait for actual response(it might be slow)
					} catch (InterruptedException ex)
					{
						ex.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++)
					{
						ServerClient c = clients.get(i);
						if (!clientResponse.contains(c.getID()))
						{
							if (c.attempt >= MAX_ATTEMPTS)
							{
								disconnect(c.getID(), false);
							} else
							{
								c.attempt++;
							}
						} else
						{
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}

				}
			}
		};
		manage.start();
	}

	private void receive()
	{
		receive = new Thread("Receive")
		{
			public void run()
			{
				while (running)
				{
					// Receiving data
					System.out.println(clients.size() + "\n");
					byte[] data = new byte[1024];
					String text = null;
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try
					{
						socket.receive(packet);
					} catch (IOException ex)
					{
						ex.printStackTrace();
					}
					process(packet);
					text = new String(packet.getData());
					System.out.println(text); // prints messages to syso
				}
			}
		};
		receive.start();
	}

	private void sendToAll(String message)
	{
		for (int i = 0; i < clients.size(); i++)
		{
			ServerClient client = clients.get(i);
			send(message.getBytes(), client.address, client.port);

		}
	}

	private void send(byte[] data, InetAddress address, int port)
	{
		send = new Thread("Send")
		{
			public void run()
			{
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
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

	private void process(DatagramPacket packet)
	{
		String text = new String(packet.getData()).trim();
		if (text.startsWith("/c/"))
		{
			// UUID id = UUID.randomUUID();
			int id = UniqueIdentifier.getIdentifier();
			clients.add(new ServerClient(text.substring(3), packet.getAddress(), packet.getPort(), id));
			// System.out.println(text.substring(3) + " " + id + "\n"); // wypisuje nazwe podlaczonego hosta
			String ID = "/c/" + id;
			send(ID.getBytes(), packet.getAddress(), packet.getPort());
		} else if (text.startsWith("/m/"))
		{
			sendToAll(text);
		} else if (text.startsWith("/dc/"))
		{
			String id = text.substring(4);
			disconnect(Integer.parseInt(id), true);
		} else if (text.startsWith("/p/"))
		{
			clientResponse.add(Integer.parseInt(text.substring(3)));
		} else
		{
			System.out.println(text);
		}
	}

	private void disconnect(int id, boolean status)
	{
		ServerClient c = null;
		for (int i = 0; i < clients.size(); ++i)
		{
			if (clients.get(i).getID() == id)
			{
				c = clients.get(i);
				clients.remove(i);
				break;
			}
		}
		String message = "";
		if (status)
		{
			message = "User " + c.name + "(" + c.getID() + ") has disconnected.";
			send(("/dc/").getBytes(), c.address, c.port);
		} else
		{
			message = "User " + c.name + "(" + c.getID() + ") has timed out.";
			send(("/dc/").getBytes(), c.address, c.port);
		}
		System.out.println(message);
	}
}
