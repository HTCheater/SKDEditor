package com.chichar.skdeditor.fragments.explorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chichar.skdeditor.Const;
import com.chichar.skdeditor.R;
import com.chichar.skdeditor.activities.EditorActivity;
import com.rosstonovsky.pussyBox.PussyFile;
import com.rosstonovsky.pussyBox.PussyShell;
import com.rosstonovsky.pussyBox.PussyUser;

import java.util.ArrayList;
import java.util.List;

public class ExplorerFragment extends Fragment {
	@SuppressLint("StaticFieldLeak")
	private static Context explorerContext;
	@SuppressLint("StaticFieldLeak")
	private static ListView explorer;
	@SuppressLint("StaticFieldLeak")
	private static PussyFile currentFolder;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private static View view;

	@SuppressLint("SdCardPath")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_explorer, container, false);
		explorer = view.findViewById(R.id.explorer);
		explorerContext = requireContext();

		PussyFile skDir = new PussyFile(PussyUser.getDataFolder() + "/" + Const.pkg);

		if (skDir.exists()) {
			openInExplorer(skDir.getAbsolutePath());
			return view;
		}
		TextView error = view.findViewById(R.id.error);
		error.setText("Soul Knight isn't installed");
		error.setVisibility(View.VISIBLE);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public static void openInExplorer(String path) {
		SharedPreferences prefs = explorerContext.getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE);
		boolean clearGarbage = prefs.getBoolean("clearGarbage", true);
		ArrayList<ExplorerItem> explorerFiles = new ArrayList<>();
		PussyFile currFolder = new PussyFile(path);

		if (!currFolder.exists()) {
			Toast.makeText(explorerContext, "Doesn't exist", Toast.LENGTH_SHORT).show();
			return;
		}

		if (currFolder.isFile()) {
			if (currFolder.length() < 2000000) {
				Intent intent = new Intent(explorerContext, EditorActivity.class);
				intent.putExtra("path", currFolder.getAbsolutePath());
				explorerContext.startActivity(intent);
				return;
			}
			Toast.makeText(explorerContext, "File is too long", Toast.LENGTH_SHORT).show();
			return;
		}

		if (path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}


		String name = "";
		String parent = "";
		for (int i = path.length() - 1; i != 0; i--) {
			if (path.charAt(i) == '/') {
				name = path.substring(i + 1);
				parent = path.substring(0, i);
				break;
			}
		}

		assert !name.equals("") : "Failed to get name";

		if (!(name.equals(Const.pkg))) {
			explorerFiles.add(new ExplorerItem(parent));
		}
		ArrayList<String> gameFiles = new ArrayList<>();
		gameFiles.add("files");
		gameFiles.add("shared_prefs");
		gameFiles.addAll(Const.gameFilesPaths.keySet());
		List<String> files = new PussyShell().busybox("ls -1 -p \"" + path + "\"");
		List<ExplorerItem> sortedFolders = new ArrayList<>();
		List<ExplorerItem> sortedFiles = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			String currName = files.get(i);
			if (currName.endsWith("/")) {
				String folderName = currName.substring(0, currName.length() - 1);
				if (clearGarbage) {
					if (gameFiles.contains(folderName)) {
						sortedFolders.add(new ExplorerItem(path + "/" + folderName, folderName, true, false));
					}
				} else {
					sortedFolders.add(new ExplorerItem(path + "/" + folderName, folderName, true, false));
				}
			} else {
				if (clearGarbage) {
					if (gameFiles.contains(currName)) {
						sortedFiles.add(new ExplorerItem(path + "/" + currName, currName, false, true));
					}
				} else {
					sortedFiles.add(new ExplorerItem(path + "/" + currName, currName, false, true));
				}
			}
		}
		explorerFiles.addAll(sortedFolders);
		explorerFiles.addAll(sortedFiles);
		ExplorerAdapter explorerAdapter = new ExplorerAdapter(explorerContext, explorerFiles);
		TextView textView = view.findViewById(R.id.path);
		textView.setText(currFolder.getName());
		assert explorer != null : "Explorer is null";
		explorer.setAdapter(explorerAdapter);
		currentFolder = currFolder;
	}
}
