package com.rosstonovsky.skdeditor;

import com.rosstonovsky.catbox.CatUser;

import java.util.ArrayList;
import java.util.HashMap;

public final class Const {

	public static final ArrayList<String> encryptedJsonGameFiles = new ArrayList<String>() {
		{
			add("game.data");
			add("item_data.data");
			add("item_data_backups.bytes");
			add("setting.data");
			add("statistic.data");
			add("season_data.data");
			add("season_data_backups.bytes");
			add("task.data");
		}
	};

	public static final HashMap<String, String> gameFilesPaths = new HashMap<String, String>() {
		{
			put("battles.data", filesDir + "/battles.data");
			put("game.data", filesDir + "/game.data");
			put("item_data.data", filesDir + "/item_data.data");
			put("setting.data", filesDir + "/setting.data");
			put("statistic.data", filesDir + "/statistic.data");
			put("season_data.data", filesDir + "/season_data.data");
			put("task.data", filesDir + "/task.data");
			put(pkg + ".v2.playerprefs.xml", dataDir + "/shared_prefs/" + pkg + ".v2.playerprefs.xml");
		}
	};

	public static final String pkg = "com.ChillyRoom.DungeonShooter";

	private static final String dataDir = CatUser.getDataFolder() + "/" + pkg;

	private static final String filesDir = dataDir + "/files";
}
