package com.chichar.skdeditor.fragments.backupManager;

public class BackupManagerItem {
	private final String path;
	private final String name;
	private final String details;
	private final boolean folder;

	// create constructor to set the values for all the parameters of the each single view
	public BackupManagerItem(String path, String name, String details) {
		this.name = name.replaceFirst("[.][^.]+$", "");
		this.path = path;
		this.details = details;
		this.folder = false;
	}

	public BackupManagerItem(String path, String name) {
		this.name = name;
		this.path = path;
		this.details = "";
		this.folder = true;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public String getDetails() {
		return details;
	}

	public boolean isFolder() {
		return folder;
	}
}
