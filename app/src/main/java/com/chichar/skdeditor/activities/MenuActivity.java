package com.chichar.skdeditor.activities;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.chichar.skdeditor.CrashHandler;
import com.chichar.skdeditor.R;
import com.chichar.skdeditor.fragments.SettingsFragment;
import com.chichar.skdeditor.fragments.SpooferFragment;
import com.chichar.skdeditor.fragments.backupManager.BackupManagerFragment;
import com.chichar.skdeditor.fragments.explorer.ExplorerFragment;
import com.chichar.skdeditor.utils.FileUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rosstonovsky.pussyBox.PussyShell;
import com.rosstonovsky.pussyBox.PussyUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MenuActivity extends AppCompatActivity {
	@SuppressLint("StaticFieldLeak")
	public static Context menucontext;
	private ImageView menuIcon;
	private static boolean splashShown = false;
	public static boolean browseStorage = false;
	private int position = -1;

	@SuppressLint("SdCardPath")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
		super.onCreate(savedInstanceState);
		File[] cacheFiles = getCacheDir().listFiles();

		if (cacheFiles != null) {
			for (File f : cacheFiles) {
				f.delete();
			}
		}

		splashScreen.setOnExitAnimationListener(splashScreenView -> {
			Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
			boolean crashed = getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE).getBoolean("crashed", false);
			if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < 33) {
				requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
			}
			if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
				startActivity(new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"));
				Toast.makeText(this, "Please, grant permission", Toast.LENGTH_SHORT).show();
				finish();
				return;
			} else if (Build.VERSION.SDK_INT == 29 && !getPackageManager().canRequestPackageInstalls()) {
				startActivity(new Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES", Uri.parse("package:com.chichar.skdeditor")));
				Toast.makeText(this, "Please, grant permission", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0 && Build.VERSION.SDK_INT < 33) {
				Toast.makeText(this, "Please, grant permission", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}

			if (!new File(getFilesDir() + "/bin/busybox").exists()) {
				installBusybox();
			}

			File backupsFolder = new File(getFilesDir() + "/backups");
			if (!backupsFolder.exists()) {
				backupsFolder.mkdir();
			}

			Uri uri = getIntent().getData();
			if (uri != null) {
				if (uri.getPath().endsWith(".skdb") && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
					try {
						InputStream inputStream = getContentResolver().openInputStream(uri);
						StringBuilder path = new StringBuilder(menucontext.getFilesDir().getAbsolutePath() + "/backups/" + uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1, uri.getPath().lastIndexOf('.')));
						for (int i = 1; new File(path + ".skdb").exists(); i++) {
							if (path.toString().lastIndexOf('_') != -1) {
								path.substring(0, path.toString().lastIndexOf('_'));
							}
							path.append("_").append(i);
						}
						path.append(".skdb");
						Log.d("TAG", "onCreate: " + path);
						OutputStream outputStream = new FileOutputStream(path.toString());
						new FileUtils().copyStream(inputStream, outputStream);
						Toast.makeText(menucontext, "Successfully imported backup", Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						Toast.makeText(menucontext, "There was an error while importing backup", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				} else if (!uri.getPath().endsWith(".skdb")) {
					Toast.makeText(this, "Invalid filetype", Toast.LENGTH_SHORT).show();
				}
			}
			View iconView = splashScreenView.getIconView();
			int duration = 300;
			if (splashShown) {
				duration = 0;
			}
			ViewPropertyAnimator vpa = iconView.animate().scaleX(0f).scaleY(0f).setDuration(duration);
			vpa.start();
			vpa.setListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@SuppressLint("SetTextI18n")
				@Override
				public void onAnimationEnd(Animator animator) {
					final Handler handler = new Handler();
					handler.postDelayed(() -> {
						iconView.setVisibility(View.GONE);
						splashShown = true;
						ViewGroup content = (ViewGroup) findViewById(android.R.id.content).getParent();
						int x = content.getWidth() / 2;
						int y = content.getHeight() / 2;
						splashScreenView.remove();
						setupMenu(crashed, x, y);
					}, 50);
				}
			});
		});
		menucontext = this;
		try {
			PussyShell.init(() -> Toast.makeText(menucontext, "There was an error while executing command, logfile located in /sdcard/SKDE/Log/", Toast.LENGTH_LONG).show());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!PussyShell.isRoot()) {
			Toast.makeText(this, "Root access denied", Toast.LENGTH_SHORT).show();
			PussyShell.closeStreams();
			finishAffinity();
			finish();
		}
		PussyUser.makeUser(this);
		Log.d("TAG", "s: ");
	}

	@SuppressLint({"SetWorldWritable", "SetWorldReadable"})
	private void installBusybox() {
		String[] abis = Build.SUPPORTED_ABIS;
		InputStream is = null;
		for (String abi : abis) {
			try {
				if (abi.equals("arm64-v8a")) {
					is = getAssets().open("busybox-arm64");
					break;
				}
				if (abi.equals("armeabi-v7a")) {
					is = getAssets().open("busybox-arm");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (is == null) {
			Toast.makeText(this, abis[0] + " is not supported", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		File dir = new File(getFilesDir() + "/bin");
		if (!dir.mkdir()) {
			Toast.makeText(this, "Failed to extract binaries", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		File out = new File(getFilesDir() + "/bin/busybox");
		try (FileOutputStream outputStream = new FileOutputStream(out, false)) {
			int read;
			byte[] bytes = new byte[8192];
			while ((read = is.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!(out.setExecutable(true, false) &&
				out.setReadable(true, false) &&
				out.setWritable(true, false))) {
			Toast.makeText(this, "Failed to set flags", Toast.LENGTH_SHORT).show();
			out.delete();
			finish();
		}
	}

	private final String[] tabsTitles = {
			"File manager",
			"Backup manager",
			"Spoofer",
			"Settings"
	};

	private final int[] tabsIcons = {
			R.drawable.ic_baseline_folder_24,
			R.drawable.ic_round_backup,
			R.drawable.ic_round_no_accounts_24,
			R.drawable.ic_round_settings_24
	};

	@SuppressLint("SetTextI18n")
	private void setupMenu(Boolean crashed, int x, int y) {
		float r = (float) Math.sqrt((x * x) + (y * y));
		ViewGroup content = (ViewGroup) findViewById(android.R.id.content).getParent();
		content.setVisibility(View.INVISIBLE);
		Animator circularReveal = ViewAnimationUtils.createCircularReveal(
				content,
				x,
				y,
				0,
				r);

		circularReveal.setDuration(300);
		if (crashed) {
			File sdcard = Environment.getExternalStorageDirectory();
			try {
				setContentView(R.layout.fragment_crash_handler);
				TextView tv = findViewById(R.id.textView2);
				tv.setText(new FileUtils().readFile(sdcard + "/SKDE/Logs/latest.log", StandardCharsets.UTF_8));
				tv.setMovementMethod(new ScrollingMovementMethod());
			} catch (Exception e) {
				setContentView(R.layout.fragment_crash_handler);
				((TextView) findViewById(R.id.textView2)).setText("Error while reading log:\n" + e);
			}
			content.setVisibility(View.VISIBLE);
			circularReveal.start();
			return;
		}

		setContentView(R.layout.activity_menu);

		ViewPager2 vp = findViewById(R.id.view_pager);
		PagerAdapter screenSlidePagerAdapter = new PagerAdapter(this);
		vp.setAdapter(screenSlidePagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);

		tabLayout.setSelectedTabIndicatorColor(getApplicationContext().getResources().getColor(R.color.purple_200));
		tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_TOP);
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				View view = tab.getCustomView();
				if (view == null) {
					return;
				}
				position = tab.getPosition();
				Log.d("TAG", "onTabSelected: " + position);
				invalidateOptionsMenu();
				TextView textView = view.findViewById(R.id.label);
				ImageView imageView = view.findViewById(R.id.icon);

				textView.setTextColor(getApplicationContext().getResources().getColor(R.color.purple_200));
				imageView.setColorFilter(getApplicationContext().getResources().getColor(R.color.purple_200));
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				View view = tab.getCustomView();
				assert view != null;
				TextView textView = view.findViewById(R.id.label);
				ImageView imageView = view.findViewById(R.id.icon);

				textView.setTextColor(getApplicationContext().getResources().getColor(R.color.light_gray));
				imageView.setColorFilter(getApplicationContext().getResources().getColor(R.color.light_gray));
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		new TabLayoutMediator(tabLayout, vp,
				(tab, position) -> {
					LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.nav_tab, vp, false);
					TextView textView = linearLayout.findViewById(R.id.label);
					ImageView imageView = linearLayout.findViewById(R.id.icon);
					Drawable drawable = AppCompatResources.getDrawable(this, tabsIcons[position]);

					textView.setText(tabsTitles[position]);
					imageView.setImageDrawable(drawable);
					tab.setCustomView(linearLayout);
				}
		).attach();
		content.setVisibility(View.VISIBLE);
		circularReveal.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE).getBoolean("crashed", false) && position == -1) {
			getMenuInflater().inflate(R.menu.menu_crash, menu);
			menu.getItem(0).setOnMenuItemClickListener(v -> {
				openDiscord();
				return true;
			});
			menu.getItem(1).setOnMenuItemClickListener(v -> {
				intentMenu();
				invalidateOptionsMenu();
				return true;
			});
			return true;
		}
		Log.d("TAG", "onCreateOptionsMenu: " + position);
		if (position != 1) {
			if (menu.size() == 0) {
				return true;
			}
			menu.getItem(0).setVisible(false);
			menu.getItem(1).setVisible(false);
			return true;
		}
		getMenuInflater().inflate(R.menu.menu_backup_manager, menu);
		final float scale = getResources().getDisplayMetrics().density;
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (34 * scale + 0.5f), (int) (34 * scale + 0.5f));

		Animation anim = new ScaleAnimation(
				0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setFillAfter(true); // Needed to keep the result of the animation
		anim.setDuration(150);
		if (!browseStorage) {
			menu.getItem(0).setVisible(true);
			menu.getItem(1).setVisible(false);
			ImageView imageView = new ImageView(this);
			imageView.setOnClickListener(item -> {
				BackupManagerFragment backupManagerFragment = (BackupManagerFragment) getSupportFragmentManager().getFragments().get(1);
				browseStorage = true;
				invalidateOptionsMenu();
				backupManagerFragment.browseStorage();
			});
			imageView.setImageResource(R.drawable.ic_skdb);
			imageView.setLayoutParams(layoutParams);
			menuIcon = imageView;
			menu.getItem(0).setActionView(imageView);
			menu.getItem(0).getActionView().startAnimation(anim);
			return true;
		}
		menu.getItem(0).setVisible(false);
		menu.getItem(1).setVisible(true);

		ImageView imageView = new ImageView(this);

		imageView.setOnClickListener(item -> {
			BackupManagerFragment backupManagerFragment = (BackupManagerFragment) getSupportFragmentManager().getFragments().get(1);
			browseStorage = false;
			invalidateOptionsMenu();
			backupManagerFragment.browseBackups();
		});

		imageView.setImageResource(R.drawable.ic_round_sd_card_24);
		imageView.setLayoutParams(layoutParams);
		menuIcon = imageView;
		menu.getItem(1).setActionView(imageView);
		menu.getItem(1).getActionView().startAnimation(anim);
		return true;
	}

	@Override
	public void invalidateOptionsMenu() {
		if (menuIcon == null || position == 1) {
			super.invalidateOptionsMenu();
			return;
		}

		Animation anim = new ScaleAnimation(
				1, 0, 1, 0,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setFillAfter(true); // Needed to keep the result of the animation
		anim.setDuration(200);
		menuIcon.startAnimation(anim);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				MenuActivity.super.invalidateOptionsMenu();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
	}

	private class PagerAdapter extends FragmentStateAdapter {

		public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
			super(fragmentActivity);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			if (position == 0) {
				return new ExplorerFragment();
			}
			if (position == 1) {
				return new BackupManagerFragment();
			}
			if (position == 2) {
				return new SpooferFragment();
			}
			if (position == 3) {
				return new SettingsFragment();
			}
			return new Fragment();
		}

		@Override
		public int getItemCount() {
			return tabsTitles.length;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == 0) {

			Intent intent = new Intent(this, MenuActivity.class);
			startActivity(intent);
			finish();
		}
		if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == -1) {
			Toast.makeText(this, "Please, grant permissions", Toast.LENGTH_SHORT).show();
		}
	}

	private void intentMenu() {
		SharedPreferences.Editor editor = getSharedPreferences("com.chichar.skdeditor", Context.MODE_PRIVATE).edit();
		editor.putBoolean("crashed", false);
		editor.apply();

		ViewGroup content = (ViewGroup) findViewById(android.R.id.content).getParent();

		int x = content.getWidth() - dip2px(this, 20);
		int y = content.getHeight() - dip2px(this, 20);
		float r = (float) Math.sqrt((x * x) + (y * y));

		Log.d("TAG", "intentMenu: " + x + ", " + y);

		Animator circularReveal = ViewAnimationUtils.createCircularReveal(
				content,
				x,
				y,
				r,
				0);

		circularReveal.setDuration(300);
		circularReveal.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(@NonNull Animator animation) {

			}

			@Override
			public void onAnimationEnd(@NonNull Animator animation) {
				if (Build.VERSION.SDK_INT >= 33) {
					setupMenu(false, x, y);
					return;
				}
				if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE") == 0) {
					setupMenu(false, x, y);
					return;
				}
				Toast.makeText(menucontext, "Please, grant requested permissions", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onAnimationCancel(@NonNull Animator animation) {

			}

			@Override
			public void onAnimationRepeat(@NonNull Animator animation) {

			}
		});
		circularReveal.start();
	}

	private static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private void openDiscord() {
		try {
			startActivity(new Intent("android.intent.action.VIEW").setData(Uri.parse("https://discord.gg/mwHgFXp")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (Exception e) {
			Toast.makeText(this, "No apps found to open the link", Toast.LENGTH_LONG).show();
		}
	}
}