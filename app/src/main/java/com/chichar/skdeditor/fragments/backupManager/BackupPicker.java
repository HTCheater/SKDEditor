package com.chichar.skdeditor.fragments.backupManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chichar.skdeditor.Const;
import com.chichar.skdeditor.R;
import com.chichar.skdeditor.activities.MenuActivity;
import com.chichar.skdeditor.utils.CryptUtil;
import com.chichar.skdeditor.utils.FileUtils;
import com.chichar.skdeditor.utils.XmlUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rosstonovsky.pussyBox.PussyFile;
import com.rosstonovsky.pussyBox.PussyShell;
import com.rosstonovsky.pussyBox.PussyUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//Suppress warnings for Const.gameFilesPaths.get()
@SuppressWarnings("ConstantConditions")
public class BackupPicker extends BottomSheetDialogFragment {

	private final String path;

	private final BackupManagerAdapter backupManagerAdapter;

	private final ArrayList<String> enabledOptions = new ArrayList<>();

	//pass parameter to restore backup
	BackupPicker(String path) {
		this.backupManagerAdapter = null;
		assert path != null : "Backup Picker: path must not be null";
		this.path = path;
	}

	//don't pass parameter to create new backup
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
		Handler handler = new Handler(Looper.myLooper());
		ExecutorService executor = Executors.newSingleThreadExecutor();

		executor.execute(() -> {
			ListView picker = view.findViewById(R.id.picker);
			if (!path.equals("")) {
				try {
					enabledOptions.addAll(getFileNames(path));
				} catch (IOException e) {
					e.printStackTrace();
					if (e instanceof EOFException) {
						handler.post(() -> Toast.makeText(requireContext(), "Invalid backup file", Toast.LENGTH_SHORT).show());
					} else {
						e.printStackTrace();
					}
				}
			} else {
				for (Map.Entry<String, String> entry : Const.gameFilesPaths.entrySet()) {
					if (new PussyFile(entry.getValue()).exists()) {
						enabledOptions.add(entry.getKey());
					}
				}
			}

			handler.post(() -> {
				if (enabledOptions.size() == 0) {
					Toast.makeText(MenuActivity.menucontext, "Nothing to restore", Toast.LENGTH_SHORT).show();
					super.dismiss();
					return;
				}
				requireView().findViewById(R.id.loading).setVisibility(View.GONE);
				requireView().findViewById(R.id.buttons).setVisibility(View.VISIBLE);

				Collections.sort(enabledOptions, String::compareToIgnoreCase);
				picker.setAdapter(new BackupPickerAdapter(requireContext(), enabledOptions));
				requireView().findViewById(R.id.discard).setOnClickListener(v -> super.dismiss());
				requireView().findViewById(R.id.restore).setOnClickListener(v -> {
					View progressBar = view.findViewById(R.id.loading);
					progressBar.setVisibility(View.VISIBLE);
					ArrayList<String> checkedFiles = ((BackupPickerAdapter) picker.getAdapter()).getCheckedArr();
					if (checkedFiles.size() == 0) {
						Toast.makeText(requireContext(), "You must select items to backup", Toast.LENGTH_SHORT).show();
						return;
					}

					Handler handler1 = new Handler(Looper.myLooper());
					ExecutorService executor1 = Executors.newSingleThreadExecutor();

					if (path.equals("")) {
						requireView().findViewById(R.id.buttons).setVisibility(View.INVISIBLE);
						requireView().findViewById(R.id.picker).setVisibility(View.INVISIBLE);
						setCancelable(false);

						executor1.execute(() -> {
							try {
								createBackup(checkedFiles);
							} catch (IOException | IllegalAccessException | ParserConfigurationException | SAXException e) {
								e.printStackTrace();
							}
							handler1.post(() -> {
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
								restoreBackup(checkedFiles, backup1);
								handler1.post(() -> {
									dismiss();
									Toast.makeText(getContext(), "Successfully restored from the backup", Toast.LENGTH_SHORT).show();
								});
							} catch (IOException e) {
								e.printStackTrace();
								handler1.post(() -> {
									Toast.makeText(requireContext(), "Invalid file", Toast.LENGTH_SHORT).show();
									dismiss();
									Toast.makeText(getContext(), "Successfully restored backup", Toast.LENGTH_SHORT).show();
								});
								return;
							}
							handler1.post(() -> {
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
		byte[] header = {83, 75, 68, 97, 116, 97, 66, 97, 99, 107, 117, 112, 0};
		int headerIndex = 0;
		while (headerIndex < header.length) {
			if (header[headerIndex] != fileInputStream.read()) {
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
				throw new EOFException();
			}
			b = (byte) r;
			if (b != 47) {
				sb.append((char) b);
			}
			if (b == 47) {
				if (prev == 47) {
					break;
				}
				files.add(sb.toString());
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
		byte[] header = {83, 75, 68, 97, 116, 97, 66, 97, 99, 107, 117, 112, 0};
		int headerIndex = 0;
		while (headerIndex < header.length) {
			if (header[headerIndex] != fileInputStream.read()) {
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
			if (b != 47) {
				sb.append((char) b);
			}
			if (b == 47) {
				if (prev == 47) {
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

	private void restoreBackup(ArrayList<String> checkedFiles, HashMap<String, Byte[]> backup) throws IOException {
		new PussyShell().cmd("." + PussyUser.getAppFilesFolder() + "/bin/busybox killall " + Const.pkg).exec();

		for (Map.Entry<String, Byte[]> entry : backup.entrySet()) {
			byte[] bytes = new byte[entry.getValue().length];
			for (int i = 0; i < entry.getValue().length; i++) {
				bytes[i] = entry.getValue()[i];
			}
			if (!Objects.equals(entry.getKey(), "battles.data") &&
					!Objects.equals(entry.getKey(), "game.data") &&
					!Objects.equals(entry.getKey(), "com.ChillyRoom.DungeonShooter.v2.playerprefs.xml")) {
				bytes = Base64.encode(bytes, android.util.Base64.NO_WRAP);
			}
			PussyFile pussyFile = new PussyFile(Const.gameFilesPaths.get(entry.getKey()));
			try (FileOutputStream outputStream = new FileOutputStream(pussyFile.getFile())) {
				outputStream.write(bytes);
			} catch (IOException e) {
				Toast.makeText(MenuActivity.menucontext, "Failed to write to output stream", Toast.LENGTH_SHORT).show();
			}
			pussyFile.commit();
		}
	}

	private void createBackup(ArrayList<String> checkedFiles) throws IOException, IllegalAccessException, ParserConfigurationException, SAXException {
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
		String fileName = dateFormat.format(new Date()).replace('.', '-');
		FileOutputStream fileOutputStream = new FileOutputStream(requireContext().getFilesDir().getAbsolutePath() + "/backups/" + fileName + ".skdb");
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream);
		DataOutputStream outputStream = new DataOutputStream(deflaterOutputStream);
		//length is 13
		fileOutputStream.write(new byte[]{83, 75, 68, 97, 116, 97, 66, 97, 99, 107, 117, 112, 0});
		for (String s : checkedFiles) {
			fileOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
			fileOutputStream.write(47);
		}
		fileOutputStream.write(47);
		fileOutputStream.flush();

		for (String s : checkedFiles) {
			PussyFile pussyFile = new PussyFile(Const.gameFilesPaths.get(s));
			byte[] bytes = FileUtils.readAllBytes(pussyFile.getFile().getAbsolutePath());

			//remove any account information to prevent account id leak
			if (Objects.equals(s, "com.ChillyRoom.DungeonShooter.v2.playerprefs.xml")) {
				String prefs = new String(bytes);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(new InputSource(new StringReader(prefs)));
				NodeList strings = document.getElementsByTagName("string");
				for (int index = 0; index < strings.getLength(); index++) {
					String name = "";
					if (strings.item(index).getAttributes() != null &&
							strings.item(index).getAttributes().getNamedItem("name") != null &&
							strings.item(index).getAttributes().getNamedItem("name").getNodeValue() != null) {
						name = strings.item(index).getAttributes().getNamedItem("name").getNodeValue();
					}
					if (name.equals("cloudSaveId") ||
							name.equals("account_enter_game_count_today") ||
							name.equals("accountLoginRecords")) {
						strings.item(index).getParentNode().removeChild(strings.item(index));
					}
				}
				bytes = XmlUtils.toString(document).getBytes(StandardCharsets.UTF_8);
			}
			if (Objects.equals(s, "setting.data")) {
				try {
					JSONObject settings = new JSONObject(CryptUtil.decrypt(bytes, "setting.data"));
					settings.remove("account2Birthday");
					settings.remove("account2ChangeCount");
					settings.remove("account2Name");
					settings.remove("account2PersonId");
					settings.remove("account2ThisDayTime");
					settings.remove("account2ResetAdditionDay");
					settings.remove("account2ResetPurchaseDay");
					settings.remove("account2PurchaseTotal");
					settings.remove("account2PurchaseTotal");
					settings.remove("PlayerBirthNameDatas");
					settings.remove("HasSolveOldRealNameData2NewData");
					byte[] enc = CryptUtil.encrypt(settings.toString(), "setting.data");
					bytes = Base64.decode(enc, android.util.Base64.NO_WRAP);
				} catch (JSONException e) {
					Toast.makeText(requireContext(), "Failed to remove ID from backup", Toast.LENGTH_SHORT).show();
				}
			}
			if (Objects.equals(s, "statistic.data")) {
				String statistics = CryptUtil.decrypt(bytes, "statistic.data");
				//can't parse statistics_data.data because some symbols in emails aren't escaped
				statistics = statistics.replaceFirst(",[\\n\\r].*?\"fixGP_Test\":\\d+", "");
				byte[] enc = CryptUtil.encrypt(statistics, "statistic.data");
				bytes = Base64.decode(enc, android.util.Base64.NO_WRAP);
			}
			if (Objects.equals(s, "item_data.data") ||
					Objects.equals(s, "season_data.data") ||
					Objects.equals(s, "task.data")) {
				bytes = Base64.decode(bytes, android.util.Base64.NO_WRAP);
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
		assert backupManagerAdapter != null;
		backupManagerAdapter.update();
	}
}
