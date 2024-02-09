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

public class BackupPickerAdapter extends ArrayAdapter<PickerElement> {

	private final ArrayList<PickerElement> fullArr;

	public BackupPickerAdapter(@NonNull Context context,
	                           ArrayList<PickerElement> fullArr) {
		super(context, 0, fullArr);
		this.fullArr = fullArr;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View currentItemView = convertView;

		if (currentItemView == null) {
			currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_backup_picker, parent, false);
		}
		View finalCurrentItemView = currentItemView;
		((TextView) finalCurrentItemView.findViewById(R.id.filename))
				.setText(fullArr.get(position).getGameFile().getRealName());

		CheckBox checkBox = finalCurrentItemView.findViewById(R.id.check);

		checkBox.setOnCheckedChangeListener(null);
		checkBox.setChecked(fullArr.get(position).isChecked());

		checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
				fullArr.get(position).setChecked(isChecked)
		);
		return currentItemView;
	}
}
