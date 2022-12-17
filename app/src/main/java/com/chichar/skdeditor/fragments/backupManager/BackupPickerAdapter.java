package com.chichar.skdeditor.fragments.backupManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chichar.skdeditor.R;

import java.util.ArrayList;

public class BackupPickerAdapter extends ArrayAdapter<String> {

	private final ArrayList<String> checkedArr;

	public BackupPickerAdapter(@NonNull Context context, ArrayList<String> arrayList) {
		super(context, 0, arrayList);
		checkedArr = arrayList;
	}

	public ArrayList<String> getCheckedArr() {
		return checkedArr;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View currentItemView = convertView;

		if (currentItemView == null) {
			currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_backup_picker, parent, false);
		}
		View finalCurrentItemView = currentItemView;
		((TextView) finalCurrentItemView.findViewById(R.id.filename)).setText(getItem(position));

		((CheckBox) finalCurrentItemView.findViewById(R.id.check)).setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked && !checkedArr.contains(getItem(position))) {
				checkedArr.add(getItem(position));
			}
			if (!isChecked) {
				checkedArr.remove(getItem(position));
			}
		});
		return currentItemView;
	}
}
