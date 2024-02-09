package com.chichar.skdeditor.gamefiles;

import androidx.annotation.NonNull;

public final class PlainGameFile implements IGameFile {

	private final String name;

	private final String realName;

	private final String path;

	public PlainGameFile(String name, String realName, String path) {
		this.name = name;
		this.realName = realName;
		this.path = path;
	}

	@NonNull
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRealName() {
		return realName;
	}

	@NonNull
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String decrypt(byte[] contents) {
		return new String(contents);
	}

	@Override
	public byte[] encrypt(String contents) {
		return contents.getBytes();
	}
}