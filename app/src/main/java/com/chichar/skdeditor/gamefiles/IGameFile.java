package com.chichar.skdeditor.gamefiles;

public interface IGameFile {
	String getName();

	String getRealName();

	String getPath();

	String decrypt(byte[] contents);

	byte[] encrypt(String contents);
}
