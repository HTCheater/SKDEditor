package com.chichar.skdeditor.gamefiles;

public class XORGameFile extends JSONGameFile {

	private static final byte[] key = new byte[] {
			115,
			108,
			99,
			122,
			125,
			103,
			117,
			99,
			127,
			87,
			109,
			108,
			107,
			74,
			95
	};

	public XORGameFile(String name, String realName, String path) {
		super(name, realName, path);
	}

	@Override
	public String decrypt(byte[] contents) {
		for (int i = 0; i < contents.length; i++) {
			contents[i] = (byte) (key[i % 15] ^ contents[i]);
		}
		return super.decrypt(contents);
	}

	@Override
	public byte[] encrypt(String contents) {
		byte[] data =  super.encrypt(contents);

		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (key[i % 15] ^ data[i]);
		}
		return data;
	}
}
