package com.chichar.skdeditor.gamefiles;

import androidx.annotation.NonNull;

import com.chichar.skdeditor.utils.JsonUtils;

public class JSONGameFile implements IGameFile {

	protected final String name;

	protected final String path;

	protected final String realName;

	public JSONGameFile(String name, String realName, String path) {
		this.name = name;
		this.path = path;
		this.realName = realName;
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
		return JsonUtils.prettyPrintJSON(JsonUtils.minify(new String(contents)));
	}

	@Override
	public byte[] encrypt(String contents) {
		return JsonUtils.minify(contents).getBytes();
	}
}
