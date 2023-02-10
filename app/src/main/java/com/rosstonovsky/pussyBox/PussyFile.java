package com.rosstonovsky.pussyBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PussyFile extends File {

	public PussyFile(@NonNull String pathname) {
		super(pathname);
		//new PussyShell().cmd(toyboxPath + " chattr -ia -R \"" + pathname + "\"").exec();
	}

	public PussyFile(@Nullable String parent, @NonNull String child) {
		super(parent, child);
	}

	public PussyFile(@Nullable File parent, @NonNull String child) {
		super(parent, child);
	}

	public PussyFile(@NonNull URI uri) {
		super(uri);
	}

	@Override
	public boolean isFile() {
		List<String> stdout = new PussyShell().toybox("stat -c %F \"" + getAbsolutePath() + "\"");
		if (stdout.size() != 0) {
			return stdout.get(0).contains("file");
		}
		return false;
	}

	@Override
	public boolean exists() {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd(PussyShell.getToyboxPath() + "ls \"" + getAbsolutePath() + "\"").to(stdout, stderr).exec();
		return stderr.size() == 0;
	}

	@Override
	public boolean isDirectory() {
		List<String> stdout = new PussyShell().toybox("stat -c %F \"" + getAbsolutePath() + "\"");

		if (stdout.size() != 0) {
			return stdout.get(0).contains("directory");
		}
		return false;
	}

	public boolean isLink() {
		List<String> stdout = new PussyShell().toybox("stat -c %F \"" + getAbsolutePath() + "\"");

		if (stdout.size() != 0) {
			return stdout.get(0).contains("symbolic link");
		}
		return false;

	}

	@Override
	public boolean delete() {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd(PussyShell.getToyboxPath() + "rm -rf \"" + getAbsolutePath() + "\"").to(stdout, stderr).exec();
		return stderr.size() == 0;
	}

	@Nullable
	@Override
	public PussyFile[] listFiles() {
		List<PussyFile> allFiles = new ArrayList<>();
		List<String> files = new PussyShell().toybox("ls -1 -p \"" + getAbsolutePath() + "\"");
		List<PussyFile> sortedFolders = new ArrayList<>();
		List<PussyFile> sortedFiles = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			String currName = files.get(i);
			if (currName.endsWith("/")) {
				sortedFolders.add(new PussyFile(getAbsolutePath() + "/" + currName.substring(0, currName.length() - 1)));
			} else {
				sortedFiles.add(new PussyFile(getAbsolutePath() + "/" + currName));
			}
		}
		allFiles.addAll(sortedFolders);
		allFiles.addAll(sortedFiles);
		return allFiles.toArray(new PussyFile[0]);
	}

	/**
	 * Returns permission, user id, group id
	 */
	public int[] getProperties() {
		List<String> stdout = new PussyShell().toybox("stat -c \"%a %u %g\" \"" + getAbsolutePath() + "\"");
		String[] propertiesArr = stdout.get(0).split("\\s");
		int[] properties = new int[3];
		int i = 0;
		for (String prop : propertiesArr) {
			properties[i] = Integer.parseUnsignedInt(prop);
			i++;
		}
		//should never happen
		if (properties[1] == -1) {
			properties[1] = properties[2];
		}
		if (properties[2] == -1) {
			properties[1] = 0;
			properties[2] = 0;
		}

		return properties;
	}

	public void setProperties(int[] properties) {
		PussyShell shell = new PussyShell();
		shell.toybox("chmod -R " + properties[0] + " \"" + getAbsolutePath() + "\"");
		shell.toybox("chown -R " + properties[1] + " \"" + getAbsolutePath() + "\"");
		shell.toybox("chgrp -R " + properties[2] + " \"" + getAbsolutePath() + "\"");
	}

	@Override
	public boolean createNewFile() throws IOException {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd("> \"" + getAbsolutePath() + "\"").to(stdout, stderr).exec();
		if (stderr.size() != 0) {
			StringBuilder sb = new StringBuilder();
			for (String s : stderr) {
				sb.append(s)
						.append("\n");
			}
			throw new IOException(sb.toString());
		}
		return true;
	}

	@Override
	public boolean mkdir() {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd(PussyShell.getToyboxPath() + "mkdir \"" + getAbsolutePath() + "\"").to(stdout, stderr).exec();
		return stderr.size() == 0;
	}

	@Override
	public boolean mkdirs() {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd(PussyShell.getToyboxPath() + "mkdir -p \"" + getAbsolutePath() + "\"").to(stdout, stderr).exec();
		return stderr.size() == 0;
	}

	public void copyTo(String path) throws IOException {
		PussyFile destionation = new PussyFile(path);
		copyTo(destionation);
	}

	/*
	* destination must be a folder
	 */

	public void copyTo(PussyFile destionation) throws IOException {
		if (!destionation.exists() && !destionation.mkdirs()) {
			throw new IOException("Failed to create dirs for destination file " +
					destionation.getAbsolutePath());
		}
		int[] props = destionation.getProperties();
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		//some binaries doesn't have -F option for cp, using rm -rf instead
		new PussyShell().cmd(PussyShell.getToyboxPath() + "rm -rf \"" + destionation.getAbsolutePath() + "/" + getName() + "\"").exec();
		new PussyShell().cmd(PussyShell.getToyboxPath() + "cp -r \"" + getPath() + "\" " + "\"" + destionation.getAbsolutePath() + "\"").to(stdout, stderr).exec();
		if (stderr.size() != 0) {
			StringBuilder sb = new StringBuilder();
			for (String s : stderr) {
				sb.append(s)
						.append("\n");
			}
			throw new IOException(sb.toString());
		}
		destionation.setProperties(props);
		if (stderr.size() != 0) {
			StringBuilder sb = new StringBuilder();
			for (String s : stderr) {
				sb.append(s)
						.append("\n");
			}
			throw new IOException(sb.toString());
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public File getFile() throws IOException {
		if ((PussyUser.getAppDataFolder() + "/cache/" + getName()).equals(getPath())) {
			throw new IOException("Cannot get file from temp file");
		}
		if (!exists()) {
			createNewFile();
		}
		PussyFile cacheFolder = new PussyFile(PussyUser.getAppDataFolder() + "/cache/");
		copyTo(cacheFolder);
		return new File(PussyUser.getProtectedCacheFolder(), getName());
	}

	/**
	 * In order to save changes from getFile() you need to call this method
	 */
	public void commit() throws IOException {
		PussyFile pussyFile = new PussyFile(PussyUser.getAppDataFolder() + "/cache/" + getName());
		if (!pussyFile.exists()) {
			throw new IOException("File " + getName() + " doesn't exist");
		}
		pussyFile.copyTo(getParent());
	}

	@Override
	public long lastModified() {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd(PussyShell.getToyboxPath() + "date +%s -r \"" + getAbsolutePath() + "\"").to(stdout, stderr).exec();
		if (stdout.size() != 0) {
			return Long.parseLong(stdout.get(0));
		}
		return 0;
	}

	@Override
	public long length() {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd(PussyShell.getToyboxPath() + "stat -c %s \"" + getAbsolutePath() + "\"").to(stdout, stderr).exec();
		if (stdout.size() != 0) {
			return Long.parseLong(stdout.get(0));
		}
		return 0;
	}
}
