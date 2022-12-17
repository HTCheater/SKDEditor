package com.chichar.skdeditor.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.chichar.skdeditor.R;

public class SettingsFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_settings, container, false);
		SharedPreferences prefs = requireContext().getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE);
		SwitchCompat clearGarbageSwitch = view.findViewById(R.id.clearGarbageSwitch);
		clearGarbageSwitch.setChecked(prefs.getBoolean("clearGarbage", true));

		clearGarbageSwitch.setOnClickListener((v -> {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("clearGarbage", clearGarbageSwitch.isChecked());
			editor.apply();
		}));
		view.findViewById(R.id.clearGarbage).setOnClickListener(v -> {
			clearGarbageSwitch.toggle();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("clearGarbage", clearGarbageSwitch.isChecked());
			editor.apply();
		});
		view.findViewById(R.id.discord).setOnClickListener(v -> {
			try {
				startActivity(new Intent("android.intent.action.VIEW").setData(Uri.parse("https://discord.gg/mwHgFXp")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			} catch (Exception e) {
				Toast.makeText(requireContext(), "No apps found to open the link", Toast.LENGTH_LONG).show();
			}
		});
		view.findViewById(R.id.youtube).setOnClickListener(v -> {
			try {
				startActivity(new Intent("android.intent.action.VIEW").setData(Uri.parse("https://www.youtube.com/@HTCheater")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			} catch (Exception e) {
				Toast.makeText(requireContext(), "No apps found to open the link", Toast.LENGTH_LONG).show();
			}
		});
		view.findViewById(R.id.github).setOnClickListener(v -> {
			try {
				startActivity(new Intent("android.intent.action.VIEW").setData(Uri.parse("https://github.com/HTCheater/SKDEditor")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			} catch (Exception e) {
				Toast.makeText(requireContext(), "No apps found to open the link", Toast.LENGTH_LONG).show();
			}
		});
		view.findViewById(R.id.changelog).setOnClickListener(v -> showChangelogDial());

		return view;
	}

	public void showChangelogDial() {
		AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
		builder.setTitle("Changelog");
		builder.setMessage(Html.fromHtml(
				"<p><b>3.0</b></p>" +
						"<p>Added Android 11 and later support</p>" +
						"<p>Technical improvements</p>" +
						"<p>UI improvements</p>" +
						"<p>Added spoofer. Now you can erase account data and change Android ID</p>" +
						"<p>Added backup manager. It's a new efficient and safe way to backup your data</p>" +
						"<p>Improved explorer's performance</p>" +
						"<p>Busybox no longer needs to be installed in system</p>" +
						"<p>Text editor has been reworked</p>" +
						"<p>Removed cloud save editing</p>" +

						"<p><b>2.1</b></p>" +
						"<p>Bugs fixed</p>" +
						"<p>Symlinks support</p>" +

						"<p><b>2.0</b></p>" +
						"<p>Added game files editing</p>" +
						"<p>Added crash handler</p>" +
						"<p>Added search and replace to editor</p>" +
						"<p>Added multiuser support</p>" +
						"<p>Automatic focus on text in editor</p>" +
						"<p>UI improvements</p>" +
						"<p>Code editor optimizations</p>" +
						"<p>Better root detection for legacy devices</p>" +
						"<p>OnClick listeners reworked</p>" +
						"<p>Better RTL support</p>" +
						"<p>Updated splash screen to modern implementation</p>" +
						"<p>APK size reduced</p>" +
						"<p>Fixed bug with permissions, group and owner when writing to file</p>" +
						"<p>Fixed bug with compatibility with latest google play services in cloud save editing</p>" +
						"<p>Fixed bug with wrong encoding when reading from restored file in cloud save editing</p>" +

						"<p><b>1.2</b></p>" +
						"<p>Fixed dialogs background</p>" +
						"<p>Fixed cloudsave not detecting on several devices</p>" +

						"<p><b>1.1</b></p>" +
						"<p>Fixed text color in cloudsave editor</p>" +

						"<p><b>1.0</b></p>" +
						"<p>Added cloudsave editing</p>"
		));
		builder.setCancelable(true);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		TextView msgTxt = alertDialog.findViewById(android.R.id.message);
		assert msgTxt != null;
		msgTxt.setTextColor(Color.parseColor("#ffffffff"));
		msgTxt.setMovementMethod(LinkMovementMethod.getInstance());
		alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
	}
}