package com.chichar.skdeditor.gamefiles;

import android.util.Log;

import com.chichar.skdeditor.Const;
import com.rosstonovsky.pussyBox.PussyFile;
import com.rosstonovsky.pussyBox.PussyUser;

import java.util.ArrayList;
import java.util.List;

public class GameFileResolver {
	private static final List<IGameFile> gameFiles = new ArrayList<>();

	private static final String TAG = "GameFileResolver";

	public static IGameFile getGameFile(PussyFile file) {
		for (IGameFile gameFile : gameFiles) {
			if (gameFile.getPath().equals(file.getPath()))
				return gameFile;
		}

		return new PlainGameFile(file.getName(), file.getName(), file.getPath());
	}

	public static void resolveFiles() {
		PussyFile prefs = new PussyFile(
				PussyUser.getDataFolder() + '/' + Const.pkg + "/shared_prefs/" +
						Const.pkg + ".v2.playerprefs.xml"
		);
		if (prefs.exists())
			gameFiles.add(
					new PlainGameFile("playerprefs", prefs.getName(), prefs.getPath())
			);
		PussyFile filesDir = new PussyFile(
				PussyUser.getDataFolder() + '/' + Const.pkg + "/files/"
		);
		if (!filesDir.exists()) {
			Log.d(TAG, "files directory doesn't exist");
			return;
		}
		PussyFile[] files = filesDir.listFiles();
		if (files == null) {
			Log.d(TAG, "files is null");
			return;
		}

		for (PussyFile file : files) {
			if (!file.getName().endsWith(".data"))
				continue;
			if (file.getName().startsWith("battles")) {
				gameFiles.add(new JSONGameFile("battles.data", file.getName(), file.getPath()));
				continue;
			}
			// Older versions used this name
			if (file.getName().startsWith("battle")) {
				gameFiles.add(new JSONGameFile("battle.data", file.getName(), file.getPath()));
				continue;
			}

			if (file.getName().startsWith("statistic")) {
				addEJSONFile(file, new byte[]{0x63, 0x72, 0x73, 0x74, 0x31, 0x0, 0x0, 0x0});
			} else if (file.getName().equals("game.data")) {
				gameFiles.add(new XORGameFile("game.data", file.getName(), file.getPath()));
			} else {
				// This is default key for all other .data files
				addEJSONFile(file, new byte[]{0x69, 0x61, 0x6d, 0x62, 0x6f, 0x0, 0x0, 0x0});
			}
		}
	}

	private static void addEJSONFile(PussyFile file, byte[] key) {
		String fileName = file.getName().replaceFirst("_(\\d+|LOCAL)_?.data", ".data");
		gameFiles.add(
				new EncryptedJSONGameFile(
						fileName, file.getName(), file.getPath(), key
				)
		);
	}

	public static List<IGameFile> getGameFiles() {
		return gameFiles;
	}
}
