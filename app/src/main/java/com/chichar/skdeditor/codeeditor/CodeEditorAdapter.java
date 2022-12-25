package com.chichar.skdeditor.codeeditor;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.chichar.skdeditor.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditorAdapter extends BaseAdapter {
	private final Context context;

	private String[] text;

	private int startSel = -1;

	private int endSel = -1;

	private boolean shouldResetSel = true;

	private final TextPaint textPaint = new TextPaint();

	private int currFocus = 0;

	private final ArrayList<Integer[]> highlight = new ArrayList<>();

	public CodeEditorAdapter(Context context, String[] text) {
		this.context = context;
		this.text = text;
	}

	public String[] getTextArray() {
		return text;
	}

	@Override
	public int getCount() {
		return text.length;
	}

	@Override
	public String getItem(int i) {
		return text[i];
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length; i++) {
			sb.append(text[i]);
			if (i != text.length - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public void setText(String[] text) {
		this.text = text;
		notifyDataSetChanged();
	}

	public void setHighlight(ArrayList<Integer[]> highlight) {
		this.highlight.clear();
		this.highlight.addAll(highlight);
		notifyDataSetChanged();
	}

	public void clearHighlight() {
		highlight.clear();
		notifyDataSetChanged();
	}

	public boolean replaceAll(Pattern pattern, String replacement) {
		boolean replaced = false;
		Matcher matcher = pattern.matcher(getText());
		while (matcher.find()) {
			replaced = true;
			text = matcher.replaceAll(replacement).split("\n");
		}
		notifyDataSetChanged();
		return replaced;
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View currentItemView = convertView;

		if (currentItemView == null) {
			currentItemView = LayoutInflater.from(context).inflate(R.layout.item_line, parent, false);
		}
		TextView lineNumber = currentItemView.findViewById(R.id.lineNumber);

		boolean isSelected = false;
		if ((startSel < position && position < endSel) && startSel != -1 && endSel != -1) {
			currentItemView.setBackgroundColor(Color.parseColor("#FFBB86FC"));
			lineNumber.setTextColor(Color.parseColor("#ff333333"));
			isSelected = true;
		} else {
			currentItemView.setBackgroundColor(Color.parseColor("#ff000000"));
			lineNumber.setTextColor(Color.parseColor("#ffaaaaaa"));
		}

		if (position == startSel || position == endSel) {
			currentItemView.setBackgroundColor(Color.parseColor("#FFBB86FC"));
			lineNumber.setTextColor(Color.parseColor("#ff333333"));
			isSelected = true;
		}

		String localText = text[position];
		EditText editText = currentItemView.findViewById(R.id.editableText);
		editText.setFilters(new InputFilter[]{});
		editText.setText(localText);

		if (!highlight.isEmpty() && !isSelected) {
			for (int i = 0; i < highlight.size(); i++) {
				if (highlight.get(i)[0] == position) {
					editText.getText().setSpan(new ForegroundColorSpan(Color.parseColor("#FFBB86FC")), highlight.get(i)[1], highlight.get(i)[2], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				else if (highlight.get(i)[0] > position) {
					break;
				}
			}
		}

		editText.setOnFocusChangeListener((view, hasFocus) -> {
			if (!hasFocus) {
				if (position > text.length - 1) {
					return;
				}
				String newString = editText.getText().toString();
				if (!localText.equals(newString)) {
					if (!highlight.isEmpty()) {
						((CodeEditor) parent).removeMatch(position);
					}
					String[] newContent = newString.split("\n");
					if (position == text.length - 1 && newContent.length == 1 && newString.endsWith("\n")) {
						String[] repl = new String[text.length + 1];
						System.arraycopy(text, 0, repl, 0, text.length);
						repl[text.length] = "";
						text = repl;
						currFocus = text.length;
						notifyDataSetChanged();
						parent.scrollBy(0, 1);
						return;
					}
					if (newContent.length > 0) {
						currFocus = currFocus + newContent.length - 1;
						text[position] = newString;
						StringBuilder stringBuilder = new StringBuilder();
						for (int i = 0; i < text.length; i++) {
							stringBuilder.append(text[i]);
							if (i != text.length - 1) {
								stringBuilder.append("\n");
							}
						}
						text = stringBuilder.toString().split("\n");
						notifyDataSetChanged();
						parent.scrollBy(0, 1);
						return;
					}
					text[position] = newString;
				}
			}
		});

		if (position == currFocus) {
			editText.requestFocus();
		}

		textPaint.setTextSize(editText.getTextSize());
		int width = (int) textPaint.measureText("99");
		if (text.length > 99) {
			width = (int) textPaint.measureText(String.valueOf(text.length));
		}
		lineNumber.setWidth(width);
		lineNumber.setText(Integer.toString(position + 1));

		lineNumber.setOnLongClickListener(view -> {
			if ((startSel > position || position < endSel) && endSel != -1 && startSel != -1) {
				startSel = -1;
				endSel = -1;
				notifyDataSetChanged();
				return false;
			}
			if (startSel == -1) {
				startSel = position;
				notifyDataSetChanged();
				return false;
			}
			if (position < startSel) {
				int i = startSel;
				startSel = position;
				endSel = i;
				notifyDataSetChanged();
			}
			if (position > startSel) {
				endSel = position;
				notifyDataSetChanged();
			}
			if (startSel != -1 && endSel != -1) {
				createContextMenu(view);
			}
			return false;
		});
		return currentItemView;
	}

	private void copy(Context context) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = startSel; i <= endSel; i++) {
			stringBuilder.append(text[i]);
			if (i != endSel) {
				stringBuilder.append("\n");
			}
		}

		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Data", stringBuilder.toString());
		clipboard.setPrimaryClip(clip);

	}

	private void delete() {
		if (startSel == 1 && endSel == text.length - 1) {
			text = new String[]{""};
			return;
		}

		for (int i = startSel; i <= endSel; i++) {
			text[i] = null;
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < text.length; i++) {
			if (text[i] != null) {
				stringBuilder.append(text[i]);
				if (i != text.length - 1) {
					stringBuilder.append("\n");
				}
			}
		}

		text = stringBuilder.toString().split("\n");
	}

	private void paste() {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

		if (!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
			return;
		}

		String replacement = clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim();


		for (int i = startSel; i <= endSel; i++) {
			text[i] = null;
		}

		text[startSel] = replacement;

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < text.length; i++) {
			if (text[i] != null) {
				stringBuilder.append(text[i]);
				if (i != text.length - 1) {
					stringBuilder.append("\n");
				}
			}
		}

		text = stringBuilder.toString().split("\n");
	}

	private void createContextMenu(View view) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
		popupMenu.getMenu().add("Copy");
		popupMenu.getMenu().add("Cut");
		if (clipboard.hasPrimaryClip()) {
			popupMenu.getMenu().add("Paste");
		}
		popupMenu.getMenu().add("Delete");
		if (!(startSel == 0 && endSel == text.length - 1)) {
			popupMenu.getMenu().add("Select all");
		}
		popupMenu.show();
		popupMenu.setOnMenuItemClickListener(menuItem -> {
			String option = menuItem.getTitle().toString();
			switch (option) {
				case "Copy":
					copy(view.getContext());
					return true;
				case "Delete":
					delete();
					return true;
				case "Cut":
					copy(view.getContext());
					delete();
					return true;
				case "Paste":
					paste();
					return true;
				case "Select all":
					startSel = 0;
					endSel = text.length - 1;
					shouldResetSel = false;
					notifyDataSetChanged();
					createContextMenu(view);
					return true;
				default:
					return true;
			}
		});
		popupMenu.setOnDismissListener(v -> {
			if (shouldResetSel) {
				startSel = -1;
				endSel = -1;
				notifyDataSetChanged();
				return;
			}
			shouldResetSel = true;
		});
	}
}
