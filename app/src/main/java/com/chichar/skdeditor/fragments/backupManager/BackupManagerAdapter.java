package com.chichar.skdeditor.fragments.backupManager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.chichar.skdeditor.R;
import com.chichar.skdeditor.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class BackupManagerAdapter extends ArrayAdapter<BackupManagerItem> {

	private final BackupManagerFragment backupManagerFragment;
	private final ArrayList<BackupManagerItem> backups;
	private String path;

	public BackupManagerAdapter(@NonNull BackupManagerFragment backupManagerFragment, ArrayList<BackupManagerItem> arrayList) {
		super(backupManagerFragment.requireContext(), 0, arrayList);

		this.backupManagerFragment = backupManagerFragment;
		this.backups = arrayList;
		this.path = getContext().getFilesDir().getAbsolutePath() + "/backups";
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setBackups(ArrayList<BackupManagerItem> backups) {
		this.backups.clear();
		this.backups.addAll(backups);
		notifyDataSetChanged();
	}

	public ArrayList<BackupManagerItem> getBackups() {
		return backups;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

		// convertView which is recyclable view
		View currentItemView = convertView;

		// of the recyclable view is null then inflate the custom layout for the same
		if (currentItemView == null) {
			currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_backup, parent, false);
		}

		// get the position of the view from the ArrayAdapter
		BackupManagerItem backupManagerItem = getItem(position);
		TextView name = currentItemView.findViewById(R.id.name);
		TextView details = currentItemView.findViewById(R.id.details);
		name.setText(backupManagerItem.getName());
		if (backupManagerItem.getDetails() != null) {
			details.setText(backupManagerItem.getDetails());
		}
		if (backupManagerItem.isFolder()) {
			((ImageView) currentItemView.findViewById(R.id.skdb)).setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.SRC_IN);
			((ImageView) currentItemView.findViewById(R.id.skdb)).setImageResource(R.drawable.ic_baseline_folder_24);
			currentItemView.setOnClickListener(v -> BackupManagerFragment.openInExplorer(backupManagerItem.getPath()));
			return currentItemView;
		}
		((ImageView) currentItemView.findViewById(R.id.skdb)).setImageResource(R.drawable.ic_skdb);
		((ImageView) currentItemView.findViewById(R.id.skdb)).setColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
		currentItemView.setOnClickListener(v -> createOptionsMenu(v, position));
		// then return the recyclable view
		return currentItemView;
	}

	private void createOptionsMenu(View view, int position) {
		PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
		popupMenu.getMenu().add("Restore");
		popupMenu.getMenu().add("Rename");
		popupMenu.getMenu().add("Delete");
		if (path.equals(getContext().getFilesDir().getAbsolutePath() + "/backups")) {
			popupMenu.getMenu().add("Export");
		} else {
			popupMenu.getMenu().add("Import");
		}
		popupMenu.show();
		popupMenu.setOnMenuItemClickListener(menuItem -> {
			String option = menuItem.getTitle().toString();
			switch (option) {
				case "Restore":
					new BackupPicker(backups.get(position).getPath()).show(backupManagerFragment.getChildFragmentManager(), null);
					return true;
				case "Rename":
					AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
					TextView title = new TextView(getContext());
					title.setText("Enter new name");
					title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
					title.setTypeface(title.getTypeface(), Typeface.BOLD);
					title.setPadding(dip2px(getContext(), 30), 0, 0, 0);
					alert.setCustomTitle(title);
					View linearLayout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edittext, null);
					alert.setView(linearLayout);

					alert.setPositiveButton("OK", (dialog, whichButton) -> {
						File from = new File(backups.get(position).getPath());
						File to = new File(Objects.requireNonNull(from.getParent()) + "/" + ((EditText) linearLayout.findViewById(R.id.input)).getText().toString());
						if (from.renameTo(to)) {
							Toast.makeText(getContext(), "Renamed successfully", Toast.LENGTH_SHORT).show();
							update();
							return;
						}
						Toast.makeText(getContext(), "There was an error while renaming backup", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					});

					alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());
					AlertDialog alertDialog = alert.create();
					alertDialog.show();

					alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
					alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
					return true;
				case "Delete":
					if (new File(backups.get(position).getPath()).delete()) {
						update();
						Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
						return true;
					}
					Toast.makeText(getContext(), "There was an error while deleting backup", Toast.LENGTH_SHORT).show();
					return true;
				case "Export":
					File skdeDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "SKDE");
					File backupDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SKDE/", "backups");
					if (!skdeDir.exists()) {
						if (!skdeDir.mkdir()) {
							Toast.makeText(getContext(), "Failed to create directory #1", Toast.LENGTH_SHORT).show();
							return true;
						}
					}
					if (!backupDir.exists()) {
						if (!backupDir.mkdir()) {
							Toast.makeText(getContext(), "Failed to create directory #2", Toast.LENGTH_SHORT).show();
							return true;
						}
					}
					File localBackup = new File(backups.get(position).getPath());
					File exportedBackup = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SKDE/backups/", backups.get(position).getName() + ".skdb");
					try {
						new FileUtils().copy(localBackup, exportedBackup);
					} catch (IOException e) {
						Toast.makeText(getContext(), "There was an error while exporting backup", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
						return true;
					}
					Toast.makeText(getContext(), "Exported successfully", Toast.LENGTH_SHORT).show();
					return true;
				case "Import":
					String path = backups.get(position).getPath();
					exportedBackup = new File(path);
					localBackup = new File(getContext().getFilesDir().getAbsolutePath() + "/backups/" + backups.get(position).getName() + ".skdb");
					for (int i = 1; localBackup.exists(); i++) {
						localBackup = new File(getContext().getFilesDir().getAbsolutePath() + "/backups/" + backups.get(position).getName() + "_" + i + ".skdb");
					}
					try {
						new FileUtils().copy(exportedBackup, localBackup);
					} catch (IOException e) {
						Toast.makeText(getContext(), "There was an error while importing backup", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
						return true;
					}
					Toast.makeText(getContext(), "Imported successfully", Toast.LENGTH_SHORT).show();
					return true;
				default:
					return true;
			}
		});
	}

	private static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public void update() {
		if (!Objects.equals(path, getContext().getFilesDir().getAbsolutePath() + "/backups")) {
			BackupManagerFragment.openInExplorer(path);
			return;
		}
		backups.clear();
		File[] fileBackups = new File(path).listFiles();
		assert fileBackups != null;
		Arrays.sort(fileBackups, (file1, file2) -> {
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
		for (File backup : fileBackups) {
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault());
			backups.add(new BackupManagerItem(backup.getAbsolutePath(), backup.getName(), dateFormat.format(new Date(backup.getAbsoluteFile().lastModified()))));
		}
		notifyDataSetChanged();
		if (backups.size() == 0) {
			backupManagerFragment.showError();
			return;
		}
		backupManagerFragment.hideError();
	}
}
