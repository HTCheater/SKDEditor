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

import java.util.ArrayList;

public class ExplorerAdapter extends ArrayAdapter<ExplorerItem> {

	public ExplorerAdapter(@NonNull Context context, ArrayList<ExplorerItem> arrayList) {
		super(context, 0, arrayList);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

		// convertView which is recyclable view
		View currentItemView = convertView;

		// of the recyclable view is null then inflate the custom layout for the same
		if (currentItemView == null) {
			currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_explorer, parent, false);
		}

		// get the position of the view from the ArrayAdapter
		ExplorerItem explorerItem = getItem(position);
		TextView filename = currentItemView.findViewById(R.id.filename);
		filename.setText(explorerItem.getFileName());
		if (explorerItem.isDirectory()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_baseline_folder_24);
		}
		if (explorerItem.isLink()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_round_shortcut_24);
		}
		if (explorerItem.isFile()) {
			((ImageView) currentItemView.findViewById(R.id.fileImage)).setImageResource(R.drawable.ic_round_file_24);
		}
		currentItemView.setOnClickListener(v -> onItemSelected(explorerItem));
		// then return the recyclable view
		return currentItemView;
	}

	public void onItemSelected(ExplorerItem explorerItem) {
		openInExplorer(explorerItem.getPath());
	}
}
