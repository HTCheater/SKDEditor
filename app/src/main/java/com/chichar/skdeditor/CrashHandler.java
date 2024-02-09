package com.chichar.skdeditor;

import static com.chichar.skdeditor.activities.MenuActivity.menuContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private final Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

	public void uncaughtException(@NonNull Thread t, Throwable e) {
		Writer stringBuffSync = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringBuffSync);
		e.printStackTrace(printWriter);
		String stacktrace = stringBuffSync.toString();
		printWriter.close();
		writeToFile(stacktrace);
		if (defaultUEH != null) {
			defaultUEH.uncaughtException(t, e);
		}
	}

	public void writeToFile(String currentStacktrace) {
		try {
			File skdeDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "SKDE");
			File logDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SKDE/", "Logs");
			if (!skdeDir.exists()) {
				skdeDir.mkdir();
			}
			if (!logDir.exists()) {
				logDir.mkdir();
			}
			String systemInfo =
					"============================" +
							"\nAndroid version: " + Build.VERSION.RELEASE +
							"\nAndroid API level: " + Build.VERSION.SDK_INT +
							"\nArchitecture: " + System.getProperty("os.arch") +
							"\nManufacturer: " + Build.MANUFACTURER +
							"\nModel: " + Build.MODEL +
							"\n============================\n";
			@SuppressLint("SimpleDateFormat")
			FileWriter fileWriter = new FileWriter(new File(logDir,
					new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) +
							".log"));
			fileWriter.append(systemInfo);
			fileWriter.append(currentStacktrace);
			fileWriter.flush();
			fileWriter.close();
			FileWriter fileWriter2 = new FileWriter(new File(logDir, "latest.log"));
			fileWriter2.append(systemInfo);
			fileWriter2.append(currentStacktrace);
			fileWriter2.flush();
			fileWriter2.close();
			SharedPreferences.Editor editor = menuContext.get().getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE).edit();
			editor.putBoolean("crashed", true);
			editor.apply();
		} catch (Exception e) {
			Log.e("ExceptionHandler", e.toString());
		}
	}
}