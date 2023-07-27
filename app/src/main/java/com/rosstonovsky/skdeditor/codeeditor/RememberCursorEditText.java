package com.rosstonovsky.skdeditor.codeeditor;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RememberCursorEditText extends EditText {

	//can handle height changes only if it's static
	private static int currSel = 0;

	private int height = 0;

	public RememberCursorEditText(@NonNull Context context) {
		super(context);
	}

	public RememberCursorEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public RememberCursorEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		if (selEnd != 0) {
			currSel = selEnd;
		}
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (!focused) {
			try {
				setSelection(currSel);
			} catch (Exception ignored) {
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (height == 0) {
			height = bottom - top;
			return;
		}
		if (height != (bottom - top)) {
			height = bottom;
			int sel = currSel;
			requestFocus();
			Log.d("TAG", "onLayout sel: " + sel);
			try {
				setSelection(sel);
			} catch (Exception ignored) {

			}
			currSel = sel;
		}
	}
}
