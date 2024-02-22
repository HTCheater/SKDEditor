package com.chichar.skdeditor.fragments.backupManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chichar.skdeditor.Const;
import com.chichar.skdeditor.IdStripper;
import com.chichar.skdeditor.R;
import com.chichar.skdeditor.activities.MenuActivity;
import com.chichar.skdeditor.gamefiles.GameFileResolver;
import com.chichar.skdeditor.gamefiles.IGameFile;
import com.chichar.skdeditor.utils.FileUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rosstonovsky.pussyBox.PussyFile;
import com.rosstonovsky.pussyBox.PussyShell;

import org.json.JSONException;
import org.xml.sax.SAXException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.ParserConfigurationException;

public class BackupPicker extends BottomSheetDialogFragment {

	private static final byte FILENAME_END = 47;

	private static final byte[] SKDB_HEADER_V1 = {83, 75, 68, 97, 116, 97, 66, 97, 99, 107, 117, 112, 0};

	// Older backups will have this name, which depended on package
	private static final String OLD_PREF_NAME = "com.ChillyRoom.DungeonShooter.v2.playerprefs.xml";

	private final String path;

	private final BackupManagerAdapter backupManagerAdapter;

	private final ArrayList<PickerElement> pickerElements = new ArrayList<>();

	// Pass path to restore backup
	BackupPicker(String path) {
		this.backupManagerAdapter = null;
		assert path != null : "Backup Picker: path must not be null";
		this.path = path;
	}

	// Create new backup
	BackupPicker(BackupManagerAdapter backupManagerAdapter) {
		this.backupManagerAdapter = backupManagerAdapter;
		this.path = "";
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.backup_picker, container, false);
		view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
		view.findViewById(R.id.buttons).setVisibility(View.GONE);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
		ExecutorService executor = Executors.newSingleThreadExecutor();

		executor.execute(() -> {
			ListView picker = view.findViewById(R.id.picker);
			if (!path.isEmpty()) {
				try {
					List<IGameFile> gameFiles = GameFileResolver.getGameFiles();
					for (String s : getFileNames(path)) {
						if (OLD_PREF_NAME.equals(s))
							s = "playerprefs";
						for (IGameFile gameFile : gameFiles)
							if (gameFile.getName().equals(s)) {
								pickerElements.add(new PickerElement(gameFile));
							}
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (e instanceof EOFException) {
						handler.post(() -> Toast.makeText(requireContext(), "Invalid backup file", Toast.LENGTH_SHORT).show());
					}
				}
				Collections.sort(pickerElements, (gf1, gf2) ->
						gf1.getGameFile().getRealName()
								.compareToIgnoreCase(gf2.getGameFile().getRealName()));
			} else {
				for (IGameFile gameFile : GameFileResolver.getGameFiles()) {
					pickerElements.add(new PickerElement(gameFile));
				}
				Collections.sort(pickerElements, (gf1, gf2) -> {
					int res = gf1.getGameFile().getName()
							.compareToIgnoreCase(gf2.getGameFile().getName());
					if (res != 0)
						return res;
					long length1 = new PussyFile(
							gf1.getGameFile().getPath()
					).length();
					long length2 = new PussyFile(
							gf2.getGameFile().getPath()
					).length();
					return Long.compare(length2, length1);
				});
				// Check the longest file among variants
				// It is most likely to be the one the user wants to backup
				for (int i = 1; i < pickerElements.size(); i++) {
					if (pickerElements.get(i - 1).getGameFile().getName()
							.equals(pickerElements.get(i).getGameFile().getName())) {
						pickerElements.get(i).setChecked(false);
					}
				}
			}
			Collections.sort(pickerElements, (gf1, gf2) ->
					gf1.getGameFile().getRealName()
							.compareToIgnoreCase(gf2.getGameFile().getRealName())
			);

			handler.post(() -> {
				if (pickerElements.size() == 0) {
					Toast.makeText(MenuActivity.menuContext.get(), "Nothing to restore", Toast.LENGTH_SHORT).show();
					super.dismiss();
					return;
				}
				view.findViewById(R.id.loading).setVisibility(View.GONE);
				view.findViewById(R.id.buttons).setVisibility(View.VISIBLE);

				picker.setAdapter(new BackupPickerAdapter(requireContext(), pickerElements));
				view.findViewById(R.id.discard).setOnClickListener(v -> super.dismiss());
				view.findViewById(R.id.restore).setOnClickListener(v -> {
					view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
					view.findViewById(R.id.buttons).setVisibility(View.GONE);
					view.findViewById(R.id.picker).setVisibility(View.GONE);
					boolean shouldBackup = false;
					for (PickerElement element : pickerElements)
						if (element.isChecked()) {
							shouldBackup = true;
							break;
						}
					if (!shouldBackup) {
						Toast.makeText(requireContext(), "You must select items to restore/backup", Toast.LENGTH_SHORT).show();
						return;
					}

					ExecutorService executor1 = Executors.newSingleThreadExecutor();

					if (path.equals("")) {
						setCancelable(false);
						executor1.execute(() -> {
							try {
								List<IGameFile> files = new ArrayList<>();
								for (PickerElement element : pickerElements)
									if (element.isChecked())
										files.add(element.getGameFile());
								createBackup(files);
							} catch (IOException | IllegalAccessException |
							         ParserConfigurationException | SAXException e) {
								e.printStackTrace();
							}
							handler.post(() -> {
								dismiss();
								update();
								Toast.makeText(getContext(), "Successfully created backup", Toast.LENGTH_SHORT).show();
							});
						});
						return;
					}
					try {
						executor1.execute(() -> {
							try {
								HashMap<String, Byte[]> backup1 = readBackup(path);
								restoreBackup(backup1);
								handler.post(() -> {
									dismiss();
									Toast.makeText(getContext(), "Successfully restored from the backup", Toast.LENGTH_SHORT).show();
								});
							} catch (IOException e) {
								e.printStackTrace();
								handler.post(() -> {
									Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
									dismiss();
								});
								return;
							}
							handler.post(() -> {
								dismiss();
								Toast.makeText(getContext(), "Successfully restored backup", Toast.LENGTH_SHORT).show();
							});
						});

					} catch (Exception e) {
						Toast.makeText(requireContext(), "There was an exception while restoring backup", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}

				});
			});
		});
	}

	private HashMap<String, Byte[]> readBackup(String path) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(path);
		InflaterInputStream inflaterInputStream = new InflaterInputStream(fileInputStream);
		DataInputStream inputStream = new DataInputStream(inflaterInputStream);
		int headerIndex = 0;
		while (headerIndex < SKDB_HEADER_V1.length) {
			if (SKDB_HEADER_V1[headerIndex] != fileInputStream.read()) {
				throw new IOException("Invalid file");
			}
			headerIndex++;
		}
		HashMap<String, Byte[]> backup = new HashMap<>();

		List<String> files = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		byte prev = 0;
		byte b;
		while (true) {
			int r = fileInputStream.read();
			if (r == -1) {
				throw new EOFException("File is corrupted");
			}
			b = (byte) r;
			if (b != FILENAME_END) {
				sb.append((char) b);
			}
			if (b == FILENAME_END) {
				if (prev == FILENAME_END) {
					break;
				}
				String str = sb.toString();
				if (OLD_PREF_NAME.equals(str))
					str = "playerprefs";
				files.add(str);
				sb.delete(0, sb.length());
			}
			prev = b;
		}

		for (int index = 0; index < files.size(); index++) {
			int len = inputStream.readInt();
			Byte[] bytes = new Byte[len];
			for (int i = 0; i < len; i++) {
				bytes[i] = inputStream.readByte();
			}
			backup.put(files.get(index), bytes);
		}

		inflaterInputStream.close();
		inputStream.close();
		fileInputStream.close();
		return backup;
	}

	private List<String> getFileNames(String path) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(path);
		DataInputStream inputStream = new DataInputStream(fileInputStream);

		int headerIndex = 0;
		while (headerIndex < SKDB_HEADER_V1.length) {
			if (SKDB_HEADER_V1[headerIndex] != fileInputStream.read()) {
				throw new IOException("Invalid file");
			}
			headerIndex++;
		}
		List<String> files = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		byte prev = 0;
		byte b;
		while (true) {
			b = inputStream.readByte();
			if (b != FILENAME_END) {
				sb.append((char) b);
			}
			if (b == FILENAME_END) {
				if (prev == FILENAME_END) {
					break;
				}
				files.add(sb.toString());
				sb.delete(0, sb.length());
			}
			prev = b;
		}
		inputStream.close();
		fileInputStream.close();
		return files;
	}

	private void restoreBackup(HashMap<String, Byte[]> backup) throws IOException {
		new PussyShell().cmd(PussyShell.getToyboxPath() + "killall " + Const.pkg).exec();

		for (PickerElement element : pickerElements) {
			if (!element.isChecked())
				continue;
			// Should never happen
			if (!backup.containsKey(element.getGameFile().getName())) {
				Log.d("Backup", "error: file not found in backup" + element.getGameFile().getName());
				continue;
			}
			Log.d("Backup", "Restoring " + element.getGameFile().getName());

			Byte[] backedUp = backup.get(element.getGameFile().getName());
			if (backedUp == null)
				continue;
			byte[] bytes = new byte[backedUp.length];
			for (int i = 0; i < backedUp.length; i++) {
				bytes[i] = backedUp[i];
			}
			if (!element.getGameFile().getName().startsWith("battle") &&
					!Objects.equals(element.getGameFile().getName(), "game.data") &&
					!Objects.equals(element.getGameFile().getName(), "playerprefs")) {
				bytes = Base64.encode(bytes, android.util.Base64.NO_WRAP);
			}
			PussyFile pussyFile = new PussyFile(element.getGameFile().getPath());
			FileOutputStream outputStream = new FileOutputStream(pussyFile.getFile());
			outputStream.write(bytes);
			outputStream.close();
			pussyFile.commit();
		}
	}

	private void createBackup(List<IGameFile> checkedFiles) throws IOException, IllegalAccessException, ParserConfigurationException, SAXException {
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
		String fileName = dateFormat.format(new Date())
				.replace('.', '-')
				.replace('/', '-')
				.replace('\\', '-');
		FileOutputStream fileOutputStream = new FileOutputStream(requireContext().getFilesDir().getAbsolutePath() + "/backups/" + fileName + ".skdb");
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream);
		DataOutputStream outputStream = new DataOutputStream(deflaterOutputStream);
		fileOutputStream.write(SKDB_HEADER_V1);
		for (IGameFile gameFile : checkedFiles) {
			fileOutputStream.write(gameFile.getName().getBytes(StandardCharsets.UTF_8));
			fileOutputStream.write(FILENAME_END);
		}
		fileOutputStream.write(FILENAME_END);
		fileOutputStream.flush();

		for (IGameFile gameFile : checkedFiles) {
			PussyFile pussyFile = new PussyFile(gameFile.getPath());
			byte[] bytes = null;

			//remove any account information
			switch (gameFile.getName()) {
				case "playerprefs":
					try {
						bytes = IdStripper.stripPrefs(pussyFile.getFile()).getBytes(StandardCharsets.UTF_8);
					} catch (IOException | JSONException e) {
						e.printStackTrace();
					}
					break;
				case "setting.data":
					try {
						bytes = IdStripper.stripSettings(pussyFile.getFile(), gameFile);
					} catch (IOException | JSONException e) {
						e.printStackTrace();
					}
					bytes = Base64.decode(bytes, android.util.Base64.NO_WRAP);
					break;
				case "statistic.data":
					bytes = IdStripper.stripStatistics(pussyFile.getFile(), gameFile);
					String statistics = gameFile.decrypt(bytes);
					//can't parse statistics_data.data because some symbols in emails aren't escaped
					statistics = statistics.replaceFirst(",[\\n\\r].*?\"fixGP_Test\":\\d+", "");
					byte[] enc = gameFile.encrypt(statistics);
					bytes = Base64.decode(enc, android.util.Base64.NO_WRAP);
					break;
				case "item_data.data":
				case "season_data.data":
				case "task.data":
					bytes = FileUtils.readAllBytes(pussyFile.getFile().getAbsolutePath());
					bytes = Base64.decode(bytes, android.util.Base64.NO_WRAP);
					break;
				default:
					bytes = FileUtils.readAllBytes(pussyFile.getFile().getAbsolutePath());
					break;
			}
			if (bytes == null) {
				outputStream.close();
				deflaterOutputStream.close();
				fileOutputStream.close();
				throw new RuntimeException("bytes length is 0");
			}
			outputStream.writeInt(bytes.length);
			outputStream.write(bytes);
			outputStream.flush();
		}
		outputStream.close();
		deflaterOutputStream.close();
		fileOutputStream.close();
	}

	private void update() {
		if (backupManagerAdapter != null)
			backupManagerAdapter.update();
	}
}
