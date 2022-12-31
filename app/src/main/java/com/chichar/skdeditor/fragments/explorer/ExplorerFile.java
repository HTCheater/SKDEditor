package com.chichar.skdeditor.fragments.explorer;

public class ExplorerFile {
	private final String name;
	private final String path;
	private final boolean isFile;
	private final boolean isDirectory;
	private final boolean isLink;

	ExplorerFile(String name, String path, boolean isFile, boolean isDirectory, boolean isLink) {
		this.name = name;
		this.path = path;
		this.isFile = isFile;
		this.isDirectory = isDirectory;
		this.isLink = isLink;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public boolean isFile() {
		return isFile;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public boolean isLink() {
		return isLink;
	}
}
