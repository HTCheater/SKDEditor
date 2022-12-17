package com.chichar.skdeditor.fragments.explorer;

import com.rosstonovsky.pussyBox.PussyFile;

public class ExplorerItem {
	private final String name;
	private final String path;
	private final boolean isFile;
	private final boolean isLink;
	private final boolean isDirectory;

	// create constructor to set the values for all the parameters of the each single view
	public ExplorerItem(String path, String name, boolean isDirectory, boolean isFile) {
		this.name = name;
		this.path = path;
		this.isDirectory =isDirectory;
		if (isFile) {
			boolean isLink = new PussyFile(path).isLink();
			if (isLink) {
				this.isLink = true;
				this.isFile = false;
				return;
			}
		}
		this.isFile = isFile;
		this.isLink = false;
	}

	public ExplorerItem(String parentPath) {
		name = "...";
		path = parentPath;
		isDirectory = true;
		isFile = false;
		isLink = false;
	}

	public String getFileName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public boolean isFile() {
		return isFile;
	}

	public boolean isLink() {
		return isLink;
	}

}
