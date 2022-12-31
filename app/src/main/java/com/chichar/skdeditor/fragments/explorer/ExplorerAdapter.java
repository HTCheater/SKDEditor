package com.chichar.skdeditor.fragments.explorer;

import static com.chichar.skdeditor.fragments.explorer.ExplorerFragment.openInExplorer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chichar.skdeditor.R;
import com.rosstonovsky.pussyBox.PussyFile;

import java.util.ArrayList;

public class ExplorerAdapter extends ArrayAdapter<PussyFile> {
	private String parent;

	public ExplorerAdapter(@NonNull Context context, ArrayList<PussyFile> arrayList, String parent) {
		super(context, 0, arrayList);
		this.parent = parent;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View currentItemView = convertView;
		if (currentItemView == null) {
			currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_explorer, parent, false);
		}
		PussyFile pussyFile = getItem(position);
		TextView filename = currentItemView.findViewById(R.id.filename);
		currentItemView.setOnClickListener(v -> onItemSelected(pussyFile));
		if (pussyFile == null) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_baseline_folder_24);
			filename.setText("...");
			return currentItemView;
		}
		filename.setText(pussyFile.getName());
		if (pussyFile.isDirectory()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_baseline_folder_24);
		}
		if (pussyFile.isLink()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_round_shortcut_24);
		}
		if (pussyFile.isFile()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_round_file_24);
		}
		return currentItemView;
	}

	private void onItemSelected(PussyFile pussyFile) {
		if (pussyFile == null) {
			openInExplorer(parent);
			return;
		}
		openInExplorer(pussyFile.getPath());
	}
}
