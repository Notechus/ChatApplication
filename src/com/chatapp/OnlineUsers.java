package com.chatapp;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will hold Online users for client
 * 
 * @author notechus
 *
 */
public class OnlineUsers
{
	public List<User> users;

	public OnlineUsers()
	{
		users = new ArrayList<>();
	}

	public void add(User user)
	{
		users.add(user);
	}

	public void remove(User user)
	{
		users.remove(user);
	}

	public static void update()
	{

	}
}
