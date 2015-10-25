package com.chatapp;

import java.net.InetAddress;

/**
 * User class
 * 
 * @author notechus
 *
 */
public class User
{
	/** Name and address of user */
	private String name, address;
	// private int port;
	/** User ID */
	private int ID = -1;
	// public boolean connected = false; usable later
	// private InetAddress ip; i probably dont need it here

	public User(String name_, String address_, int ID_)
	{
		this.name = name_;
		this.address = address_;
		this.ID = ID_;
	}
}
