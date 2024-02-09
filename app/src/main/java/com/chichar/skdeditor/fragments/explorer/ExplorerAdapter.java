package com.chichar.skdeditor.fragments.explorer;

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

import java.util.ArrayList;

public class ExplorerAdapter extends ArrayAdapter<ExplorerFile> {
	private final IOnItemSelected onItemSelected;

	public ExplorerAdapter(@NonNull Context context, ArrayList<ExplorerFile> arrayList, IOnItemSelected onItemSelected) {
		super(context, 0, arrayList);
		this.onItemSelected = onItemSelected;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View currentItemView = convertView;
		if (currentItemView == null) {
			currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_explorer, parent, false);
		}
		ExplorerFile pussyFile = getItem(position);
		if (pussyFile == null)
			return currentItemView;
		TextView filename = currentItemView.findViewById(R.id.filename);
		currentItemView.setOnClickListener(v -> onItemSelected.onItemSelected(pussyFile.getPath()));
		filename.setText(pussyFile.getName());
		if (pussyFile.isFile()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_round_file_24);
		} else if (pussyFile.isDirectory()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_baseline_folder_24);
		} else if (pussyFile.isLink()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_round_shortcut_24);
		}
		return currentItemView;
	}
}
