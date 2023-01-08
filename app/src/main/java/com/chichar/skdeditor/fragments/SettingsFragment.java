package com.chichar.skdeditor.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.chichar.skdeditor.R;
import com.rosstonovsky.pussyBox.PussyShell;
import com.rosstonovsky.pussyBox.PussyUser;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_settings, container, false);
		SharedPreferences prefs = requireContext().getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE);
		SwitchCompat clearGarbageSwitch = view.findViewById(R.id.clearGarbageSwitch);
		SwitchCompat useCustomToyboxSwitch = view.findViewById(R.id.customToyboxSwitch);
		View customToyboxCmd = view.findViewById(R.id.customToyboxCmd);
		TextView customToyboxCmdText = view.findViewById(R.id.customToyboxCmdText);
		String defToyboxCmd = "." + PussyUser.getAppFilesFolder() + "/bin/toybox ";
		View.OnClickListener setCustomToyboxCmd = v -> {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			TextView title = new TextView(getContext());
			title.setText("Enter custom toybox command");
			title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			title.setTypeface(title.getTypeface(), Typeface.BOLD);
			title.setPadding(dip2px(requireContext(), 30), 0, 0, 0);
			alert.setCustomTitle(title);
			View linearLayout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edittext, null);
			alert.setView(linearLayout);
			String prefCmd = prefs.getString("toybox", "").trim();
			if (prefCmd.equals(defToyboxCmd.trim())) {
				prefCmd = "";
			}
			((EditText) linearLayout.findViewById(R.id.input)).setHint("(auto)");
			((EditText) linearLayout.findViewById(R.id.input)).setText(prefCmd);

			alert.setPositiveButton("OK", null);


			alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());
			AlertDialog alertDialog = alert.create();
			alertDialog.show();
			alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
				String cmd = ((EditText) linearLayout.findViewById(R.id.input)).getText().toString().trim();
				if (!cmd.equals("")) {
					cmd += " ";
				}
				List<String> stdout = new ArrayList<>();
				List<String> stderr = new ArrayList<>();
				if (!cmd.equals("")) {
					new PussyShell().cmd(cmd).to(stdout, stderr).exec();
					if (stderr.size() > 0) {
						Toast.makeText(requireContext(), "Error:\n" + stderr.get(0), Toast.LENGTH_LONG).show();
						return;
					}
					stdout.clear();
				}
				//test if it's needed binary
				new PussyShell().cmd(cmd + "stat -c \"%a %u %g\" /dev/random").to(stdout, stderr).exec();
				if (stderr.size() > 0) {
					Toast.makeText(requireContext(), "Error: file is binary but stat applet doesn't exist", Toast.LENGTH_LONG).show();
					return;
				}
				SharedPreferences.Editor editor = prefs.edit();
				alertDialog.dismiss();
				if (cmd.equals("")) {
					editor.putString("toybox", "");
					editor.apply();
					return;
				}
				editor.putString("toybox", cmd + " ");
				editor.apply();
			});

			alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
		};
		clearGarbageSwitch.setChecked(prefs.getBoolean("clearGarbage", true));
		useCustomToyboxSwitch.setChecked(!prefs.getString("toybox",
						defToyboxCmd)
				.equals(defToyboxCmd));

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
		useCustomToyboxSwitch.setOnClickListener((v -> {
			if (useCustomToyboxSwitch.isChecked()) {
				customToyboxCmdText.setTextColor(getResources().getColor(R.color.white));
				customToyboxCmd.setOnClickListener(setCustomToyboxCmd);
				return;
			}
			customToyboxCmdText.setTextColor(getResources().getColor(R.color.gray));
			customToyboxCmd.setOnClickListener(v1 -> {
			});
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("toybox", defToyboxCmd);
			editor.apply();
		}));
		view.findViewById(R.id.customToybox).setOnClickListener(v -> {
			useCustomToyboxSwitch.toggle();
			if (useCustomToyboxSwitch.isChecked()) {
				customToyboxCmdText.setTextColor(getResources().getColor(R.color.white));
				customToyboxCmd.setOnClickListener(setCustomToyboxCmd);
				return;
			}
			customToyboxCmdText.setTextColor(getResources().getColor(R.color.gray));
			customToyboxCmd.setOnClickListener(v1 -> {
			});
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("toybox", defToyboxCmd);
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

		if (useCustomToyboxSwitch.isChecked()) {
			customToyboxCmd.setOnClickListener(setCustomToyboxCmd);
			return view;
		}
		customToyboxCmdText.setTextColor(getResources().getColor(R.color.gray));
		return view;
	}

	public void showChangelogDial() {
		AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
		builder.setTitle("Changelog");
		builder.setMessage(Html.fromHtml(
				"<p><b>3.1</b></p>" +
						"<p>Added ability to select custom toybox command</p>" +
						"<p>Fixed bugs</p>" +
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

	private static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}