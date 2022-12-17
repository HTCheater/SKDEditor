package com.chichar.skdeditor.codeeditor;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class RememberCursorEditText extends AppCompatEditText {

	private int currSel = 0;

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
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
		try {
			super.setSelection(currSel);
		} catch (Exception ignored) {

		}
		return true;
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (!focused) {
			try {
				super.setSelection(currSel);
			} catch (Exception ignored) {

			}
		}
	}
}
