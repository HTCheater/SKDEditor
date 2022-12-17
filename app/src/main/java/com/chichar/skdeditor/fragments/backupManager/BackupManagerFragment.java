package com.chichar.skdeditor.fragments.backupManager;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chichar.skdeditor.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rosstonovsky.pussyBox.PussyFile;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupManagerFragment extends Fragment {
	@SuppressLint("StaticFieldLeak")
	private static View view;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("SdCardPath")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_backup_manager, container, false);
		BackupManagerFragment.view = view;
		TextView pathView = view.findViewById(R.id.path);
		view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
		pathView.setText("Backups");

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.myLooper());
		executor.execute(() -> {
			File[] backups = new File(requireContext().getFilesDir().getAbsolutePath() + "/backups").listFiles();
			assert backups != null;
			Arrays.sort(backups, (file1, file2) -> {
						long k = file1.lastModified() - file2.lastModified();
						if (k > 0) {
							return 1;
						} else if (k == 0) {
							return 0;
						} else {
							return -1;
						}
					}
			);
			ListView listView = requireView().findViewById(R.id.backups);
			ArrayList<BackupManagerItem> backupsList = new ArrayList<>();
			BackupManagerAdapter backupManagerAdapter = new BackupManagerAdapter(this, backupsList);

			handler.post(() -> {
				view.findViewById(R.id.createBackup).setOnClickListener(v -> {
					BackupPicker backupPicker = new BackupPicker(backupManagerAdapter);
					backupPicker.show(requireActivity().getSupportFragmentManager(), null);
				});
				for (File backup : backups) {
					Log.d("TAG", "Detected backup: " + backup.getName());
					DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault());
					backupsList.add(new BackupManagerItem(backup.getAbsolutePath(), backup.getName(), dateFormat.format(new Date(backup.getAbsoluteFile().lastModified()))));
				}
				listView.setAdapter(backupManagerAdapter);
				if (backups.length == 0) {
					view.findViewById(R.id.error).setVisibility(View.VISIBLE);
				}

				view.findViewById(R.id.loading).setVisibility(View.GONE);
			});
		});
	}

	public void browseStorage() {
		((FloatingActionButton) view.findViewById(R.id.createBackup)).hide();
		hideError();
		openInExplorer(Environment.getExternalStorageDirectory().getAbsolutePath());
	}

	public static void openInExplorer(String path) {
		final String[] finalPath = {path};
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.myLooper());
		executor.execute(() -> {
			PussyFile currFolder = new PussyFile(path);
			if (!currFolder.exists()) {
				Toast.makeText(view.getContext(), "Doesn't exist", Toast.LENGTH_SHORT).show();
				return;
			}

			if (finalPath[0].charAt(finalPath[0].length() - 1) == '/') {
				finalPath[0] = finalPath[0].substring(0, finalPath[0].length() - 1);
			}


			String name = "";
			for (int i = finalPath[0].length() - 1; i != 0; i--) {
				if (finalPath[0].charAt(i) == '/') {
					name = finalPath[0].substring(i + 1);
					break;
				}
			}

			assert !name.equals("") : "Failed to get name";

			ArrayList<BackupManagerItem> explorerFiles = new ArrayList<>();

			String storage = Environment.getExternalStorageDirectory().getAbsolutePath();

			if (!(finalPath[0].equals(storage))) {
				String parent = new File(finalPath[0]).getParent();
				assert parent != null : "parent path is null";
				explorerFiles.add(new BackupManagerItem(parent, "..."));
			}
			File[] fileks = new File(finalPath[0]).listFiles(file -> file.getAbsolutePath().endsWith(".skdb") && file.isFile());
			File[] dirs = new File(finalPath[0]).listFiles(File::isDirectory);
			assert fileks != null : "fileks is null";
			assert dirs != null : "dirs is null";
			Log.d("TAG", "name: " + new File(finalPath[0]).getName());
			Arrays.sort(fileks, (file1, file2) -> file1.getName().compareToIgnoreCase(file2.getName()));
			Arrays.sort(dirs, (file1, file2) -> file1.getName().compareToIgnoreCase(file2.getName()));
			List<BackupManagerItem> sortedFiles = new ArrayList<>();
			List<BackupManagerItem> sortedFolders = new ArrayList<>();
			for (File filek : fileks) {
				DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault());
				sortedFiles.add(new BackupManagerItem(filek.getAbsolutePath(), filek.getName(), dateFormat.format(new Date(filek.lastModified()))));
			}
			for (File dir : dirs) {
				sortedFolders.add(new BackupManagerItem(dir.getAbsolutePath(), dir.getName()));
			}
			explorerFiles.addAll(sortedFolders);
			explorerFiles.addAll(sortedFiles);
			ListView listView = view.findViewById(R.id.backups);
			BackupManagerAdapter backupManagerAdapter = (BackupManagerAdapter) listView.getAdapter();
			backupManagerAdapter.setPath(finalPath[0]);
			Log.d("TAG", "openInBackupsExplorer: " + backupManagerAdapter.getPath());
			handler.post(() -> {
				TextView pathView = view.findViewById(R.id.path);
				HorizontalScrollView scroll = view.findViewById(R.id.scroll);
				pathView.setText(path);
				ObjectAnimator animator = ObjectAnimator.ofInt(scroll, "scrollX", pathView.getWidth());
				animator.setDuration(600);
				animator.start();
				backupManagerAdapter.setBackups(explorerFiles);
			});
		});
	}

	public void browseBackups() {
		TextView pathView = requireView().findViewById(R.id.path);
		requireView().findViewById(R.id.loading).setVisibility(View.VISIBLE);
		pathView.setText("Backups");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.myLooper());
		executor.execute(() -> {
			File[] backups = new File(requireContext().getFilesDir().getAbsolutePath() + "/backups").listFiles();
			assert backups != null;
			Arrays.sort(backups, (file1, file2) -> {
						long k = file1.lastModified() - file2.lastModified();
						if (k > 0) {
							return 1;
						} else if (k == 0) {
							return 0;
						} else {
							return -1;
						}
					}
			);
			ListView listView = view.findViewById(R.id.backups);
			BackupManagerAdapter backupManagerAdapter = (BackupManagerAdapter) listView.getAdapter();
			ArrayList<BackupManagerItem> backupsList = new ArrayList<>();

			handler.post(() -> {
				((FloatingActionButton) view.findViewById(R.id.createBackup)).show();
				view.findViewById(R.id.createBackup).setOnClickListener(v -> {
					BackupPicker backupPicker = new BackupPicker(backupManagerAdapter);
					backupPicker.show(requireActivity().getSupportFragmentManager(), null);
				});
				for (File backup : backups) {
					Log.d("TAG", "Detected backup: " + backup.getName());
					DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault());
					backupsList.add(new BackupManagerItem(backup.getAbsolutePath(), backup.getName(), dateFormat.format(new Date(backup.getAbsoluteFile().lastModified()))));
				}
				backupManagerAdapter.setBackups(backupsList);
				backupManagerAdapter.setPath(requireContext().getFilesDir().getAbsolutePath() + "/backups");
				if (backups.length == 0) {
					view.findViewById(R.id.error).setVisibility(View.VISIBLE);
				}
				requireView().findViewById(R.id.loading).setVisibility(View.GONE);
			});
		});
	}

	public void showError() {
		requireView().findViewById(R.id.error).setVisibility(View.VISIBLE);
	}

	public void hideError() {
		requireView().findViewById(R.id.error).setVisibility(View.GONE);
	}
}
