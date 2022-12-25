package com.chichar.skdeditor;

import com.rosstonovsky.pussyBox.PussyUser;

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
			put("battles.data", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/files/battles.data");
			put("game.data", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/files/game.data");
			put("item_data.data", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/files/item_data.data");
			put("setting.data", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/files/setting.data");
			put("statistic.data", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/files/statistic.data");
			put("season_data.data", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/files/season_data.data");
			put("task.data", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/files/task.data");
			put("com.ChillyRoom.DungeonShooter.v2.playerprefs.xml", PussyUser.getDataFolder() + "/com.ChillyRoom.DungeonShooter/shared_prefs/com.ChillyRoom.DungeonShooter.v2.playerprefs.xml");
		}
	};

	public static final String pkg = "com.ChillyRoom.DungeonShooter";
}
