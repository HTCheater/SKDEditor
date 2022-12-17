package com.chichar.skdeditor.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.chichar.skdeditor.R;
import com.chichar.skdeditor.codeeditor.CodeEditor;
import com.chichar.skdeditor.utils.CryptUtil;
import com.chichar.skdeditor.utils.FileUtils;
import com.rosstonovsky.pussyBox.PussyFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EditorActivity extends AppCompatActivity {

	private long lastModified = 0;
	private PussyFile pussyFile;
	private File file;
	private boolean decrypted = false;
	private boolean hideMenu = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_code_editor);

		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		assert path != null : "Path must be not null";

		pussyFile = new PussyFile(path);
		try {
			file = pussyFile.getFile();
		} catch (IOException e) {
			Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show();
			finish();
		}
		new FileUtils().readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		lastModified = file.lastModified();

		ImageButton expandFind = findViewById(R.id.expandFind);
		ImageButton onClickSave = findViewById(R.id.onClickSave);
		ImageButton openInExternalEditor = findViewById(R.id.openInExternalEditor);

		expandFind.setOnClickListener(v -> expandFind());
		onClickSave.setOnClickListener(v -> save());

		Uri uri = FileProvider.getUriForFile(this, "com.chichar.skdeditor.fileprovider", file);
		Intent test = new Intent();
		test.setAction("android.intent.action.VIEW");
		test.setDataAndType(uri, "text/json");
		test.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		test.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> result = packageManager.queryIntentActivities(test, 0);

		if (result.size() > 0) {
			openInExternalEditor.setOnClickListener(v -> openInExternalEditor());
		} else {
			openInExternalEditor.setVisibility(GONE);
		}

		TextView findInText = findViewById(R.id.findInText);
		TextView replaceInText = findViewById(R.id.replaceInText);

		findInText.setOnClickListener(v -> find());
		replaceInText.setOnClickListener(v -> replace());

		View searchbar = findViewById(R.id.cardview);
		searchbar.setScaleY(0f);
		searchbar.setVisibility(GONE);

		try {
			readsave();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ActionBar actionBar = getSupportActionBar();
		assert actionBar != null;

		actionBar.setTitle(file.getName());

		EditText editableReplaceText = findViewById(R.id.replacetext);
		EditText editableFindText = findViewById(R.id.findtext);
		CheckBox matchcase = findViewById(R.id.matchcase);
		CheckBox regex = findViewById(R.id.regex);
		SharedPreferences prefs = getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE);
		editableReplaceText.setText(prefs.getString("replace", ""));
		editableFindText.setText(prefs.getString("find", ""));
		matchcase.setChecked(prefs.getBoolean("matchCase", false));
		regex.setChecked(prefs.getBoolean("regex", false));

		if (!editableFindText.getText().toString().equals("")) {
			findInText.setTextColor(Color.parseColor("#FFBB86FC"));
			replaceInText.setTextColor(Color.parseColor("#FFBB86FC"));
		}

		editableFindText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@SuppressLint("SetTextI18n")
			@Override
			public void afterTextChanged(Editable editable) {
				findInText.setText("FIND");
				if (editable.toString().equals("")) {
					findInText.setTextColor(Color.parseColor("#FF7B46bC"));
					replaceInText.setTextColor(Color.parseColor("#FF7B46bC"));
				} else {
					findInText.setTextColor(Color.parseColor("#FFBB86FC"));
					replaceInText.setTextColor(Color.parseColor("#FFBB86FC"));
				}
			}
		});
	}

	private void readsave() throws IOException {
		findViewById(R.id.loading).setVisibility(VISIBLE);
		CodeEditor editor = findViewById(R.id.editor);
		Executor executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.myLooper());
		executor.execute(() -> {
			try {
				CryptUtil cryptUtil = new CryptUtil();
				byte[] data = new FileUtils().readAllBytes(file.getAbsolutePath());
				if (!decrypted) {
					data = cryptUtil.decrypt(data, file.getName()).getBytes(StandardCharsets.UTF_8);
					decrypted = true;
					new FileUtils().writeBytes(file, data);
				}
				byte[] finalData = data;
				handler.post(() -> {
					editor.setText(new String(finalData, StandardCharsets.UTF_8));
					findViewById(R.id.loading).setVisibility(GONE);
				});
			} catch (IOException e) {
				handler.post(() -> {
					editor.setText("");
					findViewById(R.id.loading).setVisibility(GONE);
					Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
				});
			}
		});
	}

	private void save() {
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			view.clearFocus();
		}
		findViewById(R.id.loading).setVisibility(VISIBLE);
		Executor executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.myLooper());
		String text = ((CodeEditor) findViewById(R.id.editor)).getText();
		executor.execute(() -> {
			CryptUtil cryptUtil = new CryptUtil();
			FileUtils fileUtils = new FileUtils();
			fileUtils.writeBytes(file, cryptUtil.encrypt(text, file.getName()));
			try {
				pussyFile.commit();
			} catch (IOException e) {
				handler.post(() -> {
					Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
					fileUtils.writeFile(file.getAbsolutePath(), text, StandardCharsets.UTF_8);
					findViewById(R.id.loading).setVisibility(GONE);
				});
				return;
			}
			fileUtils.writeFile(file.getAbsolutePath(), ((CodeEditor) findViewById(R.id.editor)).getText(), StandardCharsets.UTF_8);
			handler.post(() -> {
				Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
				findViewById(R.id.loading).setVisibility(GONE);
			});
		});
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.menu_editor, menu);
		if (hideMenu) {
			for (int i = 0; i < menu.size(); i++) {
				menu.getItem(i).setVisible(false);
			}
		}

		menu.getItem(0).setOnMenuItemClickListener(menuItem -> {
			CodeEditor codeEditor = findViewById(R.id.editor);
			codeEditor.findPrevMatch();
			return false;
		});

		menu.getItem(1).setOnMenuItemClickListener(menuItem -> {
			CodeEditor codeEditor = findViewById(R.id.editor);
			codeEditor.findNextMatch();
			return false;
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home && !hideMenu) {
			hideMenu = true;
			CodeEditor codeEditor = findViewById(R.id.editor);
			codeEditor.clearMatches();
			invalidateOptionsMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (lastModified < file.lastModified()) {
			try {
				readsave();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				lastModified = file.getAbsoluteFile().lastModified();
			}
		}
	}

	private void openInExternalEditor() {
		lastModified = file.getAbsoluteFile().lastModified();
		Uri uri = FileProvider.getUriForFile(this, "com.chichar.skdeditor.fileprovider", file);
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/json");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "No external text editors installed", Toast.LENGTH_SHORT).show();
		}
	}

	private void expandFind() {
		CodeEditor codeEditor = findViewById(R.id.editor);
		if (!hideMenu) {
			codeEditor.clearMatches();
			hideMenu = true;
			invalidateOptionsMenu();
		}
		View searchbar = findViewById(R.id.cardview);
		searchbar.setPivotY(0f);
		if (searchbar.getScaleY() == 0f) {
			EditText findEditText = findViewById(R.id.findtext);
			findEditText.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.showSoftInput(findEditText, InputMethodManager.SHOW_IMPLICIT);
			searchbar.animate().scaleY(1f).setDuration(200)
					.setListener(new Animator.AnimatorListener() {
						@Override
						public void onAnimationCancel(Animator animator) {

						}

						@Override
						public void onAnimationRepeat(Animator animator) {

						}

						@Override
						public void onAnimationEnd(Animator animator) {

						}

						@Override
						public void onAnimationStart(Animator animator) {
							searchbar.setVisibility(VISIBLE);
						}
					})
					.start();
		} else {
			codeEditor.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.showSoftInput(codeEditor, InputMethodManager.SHOW_IMPLICIT);
			searchbar.animate().scaleY(0f).setDuration(200).setListener(new Animator.AnimatorListener() {

						@Override
						public void onAnimationStart(Animator animator) {

						}

						@Override
						public void onAnimationCancel(Animator animator) {

						}

						@Override
						public void onAnimationRepeat(Animator animator) {

						}

						@Override
						public void onAnimationEnd(Animator animator) {
							searchbar.setVisibility(GONE);
						}
					})
					.start();
		}
	}


	@SuppressLint("SetTextI18n")
	private void find() {
		CodeEditor codeEditor = findViewById(R.id.editor);
		CheckBox regexcheck = findViewById(R.id.regex);
		CheckBox matchcasecheck = findViewById(R.id.matchcase);
		EditText editableFindText = findViewById(R.id.findtext);
		String findText = editableFindText.getText().toString();
		EditText editableReplaceText = findViewById(R.id.replacetext);
		String replaceText = editableReplaceText.getText().toString();
		boolean regex = regexcheck.isChecked();
		boolean matchcase = matchcasecheck.isChecked();

		SharedPreferences.Editor editor = getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE).edit();
		editor.putString("find", findText);
		editor.putString("replace", replaceText);
		editor.putBoolean("matchCase", matchcasecheck.isChecked());
		editor.putBoolean("regex", regexcheck.isChecked());
		editor.apply();


		Pattern pattern;
		try {
			if (regex) {
				pattern = Pattern.compile(findText);
				if (!matchcase) {
					pattern = Pattern.compile(findText, Pattern.CASE_INSENSITIVE);
				}
			} else {
				pattern = Pattern.compile(findText, Pattern.LITERAL);
				if (!matchcase) {
					pattern = Pattern.compile(findText, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
				}
			}
			codeEditor.clearMatches();
		} catch (PatternSyntaxException e) {
			Toast.makeText(this, "Regex syntax error near index " + e.getIndex(), Toast.LENGTH_SHORT).show();
			return;
		}

		if (codeEditor.findMatches(pattern).isEmpty()) {
			Toast.makeText(this, "Nothing found", Toast.LENGTH_SHORT).show();
			return;
		}

		expandFind();
		hideMenu = false;
		invalidateOptionsMenu();
	}

	private void replace() {
		CodeEditor codeEditor = findViewById(R.id.editor);
		CheckBox regexcheck = findViewById(R.id.regex);
		CheckBox matchcasecheck = findViewById(R.id.matchcase);
		EditText editableReplaceText = findViewById(R.id.replacetext);
		EditText editableFindText = findViewById(R.id.findtext);
		String replaceText = editableReplaceText.getText().toString();
		String findText = editableFindText.getText().toString();

		SharedPreferences.Editor editor = getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE).edit();
		editor.putString("find", findText);
		editor.putString("replace", replaceText);
		editor.putBoolean("matchCase", matchcasecheck.isChecked());
		editor.putBoolean("regex", regexcheck.isChecked());
		editor.apply();

		boolean regex = regexcheck.isChecked();
		boolean matchcase = matchcasecheck.isChecked();

		Pattern pattern;
		try {
			if (regex) {
				pattern = Pattern.compile(findText);
				if (!matchcase) {
					pattern = Pattern.compile(findText, Pattern.CASE_INSENSITIVE);
				}
			} else {
				pattern = Pattern.compile(findText, Pattern.LITERAL);
				if (!matchcase) {
					pattern = Pattern.compile(findText, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
				}
			}
			if (codeEditor.replaceAllMatches(pattern, replaceText)) {
				Toast.makeText(this, "Replaced", Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(this, "Nothing found", Toast.LENGTH_SHORT).show();
		} catch (PatternSyntaxException e) {
			Toast.makeText(this, "Regex syntax error near index " + e.getIndex(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}