package com.chatapp.security.authentication;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Password
{
	private final Random RANDOM;
	private static final int ITERATIONS = 1000;
	private static final int KEY_LENGTH = 256;

	public Password() throws NoSuchAlgorithmException
	{
		RANDOM = SecureRandom.getInstance("SHA1PRNG");
	}

	public byte[] getSalt()
	{
		byte[] salt = new byte[32];
		RANDOM.nextBytes(salt);
		return salt;
	}

	public byte[] hash(char[] password, byte[] salt)
	{
		PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
		Arrays.fill(password, Character.MIN_VALUE);
		try
		{
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			return skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
		} finally
		{
			spec.clearPassword();
		}
	}

	public boolean isExpectedPassword(char[] password, byte[] salt, byte[] expectedHash)
	{
		byte[] pwdHash = hash(password, salt);
		Arrays.fill(password, Character.MIN_VALUE);
		if (pwdHash.length != expectedHash.length) return false;
		for (int i = 0; i < pwdHash.length; i++)
		{
			if (pwdHash[i] != expectedHash[i]) return false;
		}
		return true;
	}

	public String generateRandomPassword(int length)
	{
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
		{
			int c = RANDOM.nextInt(62);
			if (c <= 9)
			{
				sb.append(String.valueOf(c));
			} else if (c < 36)
			{
				sb.append((char) ('a' + c - 10));
			} else
			{
				sb.append((char) ('A' + c - 36));
			}
		}
		return sb.toString();
	}
}
