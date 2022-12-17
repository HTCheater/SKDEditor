package com.rosstonovsky.pussyBox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PussyUser {

	private static String appFilesFolder;

	private static String appDataFolder;

	private static String dataFolder;

	private static int id = 0;

	public static String getAppFilesFolder() {
		return appFilesFolder;
	}

	public static String getDataFolder() {
		return dataFolder;
	}

	public static String getAppDataFolder() {
		return appDataFolder;
	}

	public static int getId() {
		return id;
	}

	@SuppressLint("SdCardPath")
	public static void makeUser(Context context) {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		new PussyShell().cmd("pm list users").to(stdout, stderr).exec();
		int id = -1;
		for (String line : stdout) {
			int i1 = line.indexOf('{');
			int i2 = line.indexOf(':');
			if (line.contains("running") && i1 != -1 && i2 != -1) {
				try {
					id = Integer.parseInt(line.substring(i1 + 1, i2));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		Log.d("TAG", "Current user: " + id);
		if (id == -1) {
			Toast.makeText(context, "Failed to get user id, using default", Toast.LENGTH_SHORT).show();
			id = 0;
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
			dataFolder = "/data_mirror/data_ce/null/" + id;
			appDataFolder = "/data_mirror/data_ce/null/" + id + "/" + context.getPackageName();
			appFilesFolder = appDataFolder + "/files";
			return;
		}
		dataFolder = "/data/user/" + id;
		appDataFolder = "/data/user/" + id + "/" + context.getPackageName();
		appFilesFolder = appDataFolder + "/files";
		PussyUser.id = id;
	}
}
