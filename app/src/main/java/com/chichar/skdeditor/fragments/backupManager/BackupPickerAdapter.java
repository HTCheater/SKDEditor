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
	private final ArrayList<String> fullArr = new ArrayList<>();

	public BackupPickerAdapter(@NonNull Context context, ArrayList<String> arrayList) {
		super(context, 0, arrayList);
		checkedArr = arrayList;
		fullArr.addAll(arrayList);
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
		((TextView) finalCurrentItemView.findViewById(R.id.filename)).setText(fullArr.get(position));

		((CheckBox) finalCurrentItemView.findViewById(R.id.check)).setOnCheckedChangeListener((buttonView, isChecked) -> {
			String item = fullArr.get(position);
			if (isChecked && !checkedArr.contains(item)) {
				checkedArr.add(item);
			}
			if (!isChecked) {
				checkedArr.remove(item);
			}
		});
		return currentItemView;
	}
}
