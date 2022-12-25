package com.chichar.skdeditor.fragments;

import static com.chichar.skdeditor.activities.MenuActivity.menucontext;
import static com.rosstonovsky.ABXUtils.ABXWriter.ATTRIBUTE;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_BOOLEAN_FALSE;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_BOOLEAN_TRUE;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_BYTES_BASE64;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_BYTES_HEX;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_DOUBLE;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_FLOAT;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_INT;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_INT_HEX;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_LONG;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_LONG_HEX;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_STRING;
import static com.rosstonovsky.ABXUtils.ABXWriter.TYPE_STRING_INTERNED;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chichar.skdeditor.Const;
import com.chichar.skdeditor.CrashHandler;
import com.chichar.skdeditor.R;
import com.chichar.skdeditor.utils.CryptUtil;
import com.chichar.skdeditor.utils.FileUtils;
import com.chichar.skdeditor.utils.XmlUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rosstonovsky.ABXUtils.ABXReader;
import com.rosstonovsky.ABXUtils.ABXWriter;
import com.rosstonovsky.pussyBox.PussyFile;
import com.rosstonovsky.pussyBox.PussyShell;
import com.rosstonovsky.pussyBox.PussyUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//Suppress warnings for Const.gameFilesPaths.get()
@SuppressWarnings("ConstantConditions")

public class SpooferFragment extends Fragment {

	private String androidId;
	private String defAndroidId;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_spoofer, container, false);
		view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
		MaterialButton accountClear = view.findViewById(R.id.account_clear);
		MaterialButton androidSpoof = view.findViewById(R.id.android_spoof);
		((FloatingActionButton) view.findViewById(R.id.reboot)).hide();

		accountClear.setOnClickListener(v -> {
			accountClear.setEnabled(false);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Handler handler = new Handler(Looper.myLooper());
			executor.execute(() -> {
				try {
					clearAccount();
					handler.post(() -> {
						readData();
						accountClear.setEnabled(true);
					});
				} catch (ParserConfigurationException | SAXException e) {
					handler.post(() -> Toast.makeText(menucontext, "Failed to parse preferences", Toast.LENGTH_SHORT).show());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					handler.post(() -> Toast.makeText(menucontext, "Failed to parse JSON", Toast.LENGTH_SHORT).show());
				}
			});
		});

		androidSpoof.setOnClickListener(v -> {
			view.findViewById(R.id.reboot).setVisibility(View.VISIBLE);
			((FloatingActionButton) view.findViewById(R.id.reboot)).show();
			androidSpoof.setEnabled(false);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Handler handler = new Handler(Looper.myLooper());
			executor.execute(() -> {
				try {
					spoofAndroidId();
				} catch (IOException e) {
					new CrashHandler().writeToFile(e.toString());
					handler.post(() -> {
						Toast.makeText(requireContext(), "Error occurred, view log for details", Toast.LENGTH_SHORT).show();
						androidSpoof.setEnabled(true);
					});
					return;
				} catch (XmlPullParserException | SAXException | ParserConfigurationException e) {
					new CrashHandler().writeToFile(e.toString());
					handler.post(() -> {
						Toast.makeText(requireContext(), "Syntax error occurred. are you sure your system is alright?", Toast.LENGTH_LONG).show();
						androidSpoof.setEnabled(true);
					});
					return;
				}
				handler.post(() -> {
					readData();
					androidSpoof.setEnabled(true);
				});
			});
		});

		view.findViewById(R.id.reboot).setOnClickListener(v -> new PussyShell().busybox("killall system_server"));

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		readData();
	}

	private void readData() {
		TextView accountIdTv = requireView().findViewById(R.id.account_id);
		TextView androidIdTv = requireView().findViewById(R.id.android_id);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Handler handler = new Handler(Looper.myLooper());
		executor.execute(() -> {
			String accountId = "Failed to get the id";
			androidId = "Failed to get the id";
			PussyFile pussyPrefs = new PussyFile(Const.gameFilesPaths.get("com.ChillyRoom.DungeonShooter.v2.playerprefs.xml"));
			if (pussyPrefs.exists()) {
				String prefs = "";
				try {
					prefs = new FileUtils().readFile(pussyPrefs.getFile().getAbsolutePath(), StandardCharsets.UTF_8);
				} catch (IOException e) {
					handler.post(() -> Toast.makeText(menucontext, "Failed to read preferences", Toast.LENGTH_SHORT).show());
				}
				Pattern pattern = Pattern.compile("name=\"cloudSaveId\">(.*?)</");
				Matcher matcher = pattern.matcher(prefs);
				if (matcher.find()) {
					accountId = matcher.group(1);
				}


				String finalAccountId = accountId;
				handler.post(() -> {
					assert finalAccountId != null;
					if (!finalAccountId.contains("Failed to")) {
						requireView().findViewById(R.id.account_clear).setEnabled(true);
					}
					accountIdTv.setText(finalAccountId);
				});
			} else {
				handler.post(() -> {
					requireView().findViewById(R.id.account_clear).setEnabled(false);
					accountIdTv.setText("Preferences don't exist");
				});
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				try {
					PussyFile pussyFile = new PussyFile("/data/system/users/" + PussyUser.getId() + "/settings_ssaid.xml");
					File file = pussyFile.getFile();
					InputStream inputStream = new FileInputStream(file);
					ABXReader abxReader = new ABXReader();
					abxReader.setInput(inputStream, StandardCharsets.UTF_8.name());

					int eventType = abxReader.getEventType();

					while (eventType != XmlPullParser.END_DOCUMENT) {
						if (eventType == XmlPullParser.END_TAG) {
							if (abxReader.getAttributeValue("package") != null &&
									abxReader.getAttributeValue("package").equals(Const.pkg)) {
								androidId = abxReader.getAttributeValue("value");
								defAndroidId = abxReader.getAttributeValue("defaultValue");
								break;
							}
						}
						eventType = abxReader.next();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				Log.d("TAG", "androidId: " + androidId);
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				try {
					PussyFile pussyFile = new PussyFile("/data/system/users/" + PussyUser.getId() + "/settings_ssaid.xml");
					String ssaidXml = new FileUtils().readFile(pussyFile.getFile().getAbsolutePath(), StandardCharsets.UTF_8);
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser parser = factory.newPullParser();
					parser.setInput(new StringReader(ssaidXml));

					int eventType = parser.getEventType();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						if (eventType == XmlPullParser.END_TAG) {
							if (parser.getAttributeValue(null, "package") != null &&
									parser.getAttributeValue(null, "package").equals(Const.pkg)) {
								androidId = parser.getAttributeValue(null, "value");
								break;
							}
						}
						eventType = parser.next();
					}
				} catch (XmlPullParserException | IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					PussyFile pussyFile = new PussyFile("/data/system/users/" + PussyUser.getId() + "/settings_secure.xml");
					String androidIdXml = new FileUtils().readFile(pussyFile.getFile().getAbsolutePath(), StandardCharsets.UTF_8);
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser parser = factory.newPullParser();
					parser.setInput(new StringReader(androidIdXml));

					int eventType = parser.getEventType();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						if (eventType == XmlPullParser.END_TAG) {
							if (parser.getAttributeValue(null, "name") != null && parser.getAttributeValue(null, "name").equals("android_id")) {
								androidId = parser.getAttributeValue(null, "value");
								break;
							}
						}
						eventType = parser.next();
					}
				} catch (XmlPullParserException | IOException e) {
					e.printStackTrace();
				}
			}

			String finalAndroidId = androidId;
			handler.post(() -> {
				assert finalAndroidId != null;
				if (!finalAndroidId.contains("Failed to")) {
					requireView().findViewById(R.id.android_spoof).setEnabled(true);
				}
				androidIdTv.setText(finalAndroidId);
				requireView().findViewById(R.id.loading).setVisibility(View.GONE);
			});
		});
	}


	private void clearAccount() throws ParserConfigurationException, IOException, SAXException, JSONException {
		PussyFile pussyPrefs = new PussyFile(Const.gameFilesPaths.get("com.ChillyRoom.DungeonShooter.v2.playerprefs.xml"));
		File prefsFile = pussyPrefs.getFile();
		String prefs = new FileUtils().readFile(prefsFile.getAbsolutePath(), StandardCharsets.UTF_8);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(prefs)));
		NodeList strings = document.getElementsByTagName("string");
		for (int index = 0; index < strings.getLength(); index++) {
			String name = strings.item(index).getAttributes().getNamedItem("name").getNodeValue();
			if (name.equals("cloudSaveId")) {
				strings.item(index).getParentNode().removeChild(strings.item(index));
				prefs = new XmlUtils().toString(document);
			}
		}
		new FileUtils().writeFile(prefsFile.getAbsolutePath(), prefs, StandardCharsets.UTF_8);
		pussyPrefs.commit();

		PussyFile pussySettings = new PussyFile(Const.gameFilesPaths.get("setting.data"));
		File settingsFile = pussySettings.getFile();
		byte[] settingsBytes = new FileUtils().readAllBytes(settingsFile.getAbsolutePath());
		CryptUtil cryptUtil = new CryptUtil();
		JSONObject settings = new JSONObject(cryptUtil.decrypt(settingsBytes, "setting.data"));
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
		byte[] enc = cryptUtil.encrypt(settings.toString(), "setting.data");
		new FileUtils().writeBytes(settingsFile, enc);
		pussySettings.commit();

		PussyFile pussyStatistics = new PussyFile(Const.gameFilesPaths.get("statistic.data"));
		File statisticsFile = pussyStatistics.getFile();
		byte[] statisticsBytes = new FileUtils().readAllBytes(statisticsFile.getAbsolutePath());
		String statistics = cryptUtil.decrypt(statisticsBytes, "statistic.data");
		//can't parse statistics_data.data because some symbols in emails aren't escaped
		statistics = statistics.replaceFirst(",[\\n\\r].*?\"fixGP_Test\":\\d+", "");
		enc = cryptUtil.encrypt(statistics, "statistic.data");
		new FileUtils().writeBytes(statisticsFile, enc);
		pussyStatistics.commit();
	}

	private void spoofAndroidId() throws IOException, XmlPullParserException, ParserConfigurationException, SAXException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			PussyFile pussyFile = new PussyFile("/data/system/users/" + PussyUser.getId() + "/settings_ssaid.xml");

			File file = pussyFile.getFile();
			ABXReader abxReader = new ABXReader();
			InputStream inputStream = new FileInputStream(file);
			abxReader.setInput(inputStream, StandardCharsets.UTF_8.name());

			File abx = new File(requireContext().getCacheDir() + "/ssaid.abx");
			ABXWriter abxWriter = new ABXWriter();
			OutputStream outputStream = new FileOutputStream(abx);
			abxWriter.setOutput(outputStream, StandardCharsets.UTF_8.name());

			abxWriter.startDocument(null, null);

			int event = abxReader.getNextEvent();
			int token = event & 0x0f;

			while (token != XmlPullParser.END_DOCUMENT) {
				token = event & 0x0f;
				final int type = event & 0xf0;
				switch (token) {
					case ATTRIBUTE:
						ABXReader.Attribute attribute = abxReader.obtainAttribute();
						attribute.name = abxReader.mIn.readInternedUTF();
						attribute.type = type;
						switch (attribute.type) {
							case TYPE_BOOLEAN_TRUE:
								abxWriter.attributeBoolean(null, attribute.name, true);
								break;
							case TYPE_BOOLEAN_FALSE:
								abxWriter.attributeBoolean(null, attribute.name, false);
								break;
							case TYPE_STRING:
								attribute.valueString = abxReader.mIn.readUTF();
								String a = attribute.valueString;
								if (Objects.equals(a, androidId) ||
										Objects.equals(a, defAndroidId)) {
									Log.d("TAG", "GOT IT");
									a = UUID.randomUUID().toString()
											.replace("-", "")
											.substring(0, 16);
								}
								abxWriter.attribute(null, attribute.name, a);
								break;
							case TYPE_STRING_INTERNED:
								attribute.valueString = abxReader.mIn.readInternedUTF();
								Log.d("TAG", "GOT IT");
								a = attribute.getValueString();
								if (Objects.equals(a, androidId) ||
										Objects.equals(a, defAndroidId)) {
									a = UUID.randomUUID().toString()
											.replace("-", "")
											.substring(0, 16);
								}
								abxWriter.attributeInterned(null, attribute.name, attribute.getValueString());
								break;
							case TYPE_BYTES_HEX:
								int len = abxReader.mIn.readUnsignedShort();
								byte[] res = new byte[len];
								abxReader.mIn.readFully(res);
								attribute.valueBytes = res;
								abxWriter.attributeBytesHex(null, attribute.name, attribute.getValueBytesHex());
								break;
							case TYPE_BYTES_BASE64:
								len = abxReader.mIn.readUnsignedShort();
								res = new byte[len];
								abxReader.mIn.readFully(res);
								attribute.valueBytes = res;
								abxWriter.attributeBytesBase64(null, attribute.name, attribute.getValueBytesBase64());
								break;
							case TYPE_INT:
								attribute.valueInt = abxReader.mIn.readInt();
								abxWriter.attributeInt(null, attribute.name, attribute.getValueInt());
								break;
							case TYPE_INT_HEX:
								attribute.valueInt = abxReader.mIn.readInt();
								abxWriter.attributeIntHex(null, attribute.name, attribute.getValueIntHex());
								break;
							case TYPE_LONG:
								attribute.valueLong = abxReader.mIn.readLong();
								abxWriter.attributeLong(null, attribute.name, attribute.getValueLong());
								break;
							case TYPE_LONG_HEX:
								attribute.valueLong = abxReader.mIn.readLong();
								abxWriter.attributeLongHex(null, attribute.name, attribute.getValueLongHex());
								break;
							case TYPE_FLOAT:
								attribute.valueFloat = abxReader.mIn.readFloat();
								abxWriter.attributeFloat(null, attribute.name, attribute.getValueFloat());
								break;
							case TYPE_DOUBLE:
								attribute.valueDouble = abxReader.mIn.readDouble();
								abxWriter.attributeDouble(null, attribute.name, attribute.getValueDouble());
								break;
							default:
								throw new IOException("Unexpected data type " + attribute.type);
						}
						break;
					case XmlPullParser.END_DOCUMENT:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = null;
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.endDocument();
						break;
					case XmlPullParser.START_DOCUMENT:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = null;
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.startDocument(null, null);
						break;
					case XmlPullParser.START_TAG:
						abxReader.mCurrentName = abxReader.mIn.readInternedUTF();
						abxReader.mCurrentText = null;
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.startTag(null, abxReader.getName());
						break;
					case XmlPullParser.END_TAG:
						abxReader.mCurrentName = abxReader.mIn.readInternedUTF();
						abxReader.mCurrentText = null;
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.endTag(null, abxReader.getName());
						break;
					case XmlPullParser.TEXT:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = abxReader.mIn.readUTF();
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.text(abxReader.getText());
						break;
					case XmlPullParser.CDSECT:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = abxReader.mIn.readUTF();
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.cdsect(abxReader.getText());
						break;
					case XmlPullParser.PROCESSING_INSTRUCTION:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = abxReader.mIn.readUTF();
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.processingInstruction(abxReader.getText());
						break;
					case XmlPullParser.COMMENT:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = abxReader.mIn.readUTF();
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.comment(abxReader.getText());
						break;
					case XmlPullParser.DOCDECL:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = abxReader.mIn.readUTF();
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.docdecl(abxReader.getText());
						break;
					case XmlPullParser.IGNORABLE_WHITESPACE:
						abxReader.mCurrentName = null;
						abxReader.mCurrentText = abxReader.mIn.readUTF();
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.ignorableWhitespace(abxReader.getText());
						break;
					case XmlPullParser.ENTITY_REF:
						abxReader.mCurrentName = abxReader.mIn.readUTF();
						abxReader.mCurrentText = ABXReader.resolveEntity(abxReader.mCurrentName);
						if (abxReader.getAttributeCount() > 0) abxReader.resetAttributes();
						abxWriter.entityRef(abxReader.getText());
						break;
					default: {
						throw new IOException("Unknown token " + token + " with type " + type);
					}
				}
				if (token != XmlPullParser.END_DOCUMENT) {
					event = abxReader.getNextEvent();
				}
			}
			inputStream.close();
			outputStream.close();
			Files.delete(file.toPath());
			if (abx.renameTo(file)) {
				pussyFile.commit();
				return;
			}
			throw new IOException("Failed to rename file");
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			PussyFile pussySSAID = new PussyFile("/data/system/users/" + PussyUser.getId() + "/settings_ssaid.xml");
			File fileSSAID = pussySSAID.getFile();
			String ssaidXml = new FileUtils().readFile(fileSSAID.getAbsolutePath(), StandardCharsets.UTF_8);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(ssaidXml)));
			NodeList packages = document.getElementsByTagName("setting");
			for (int index = 0; index < packages.getLength(); index++) {
				String packageName = packages.item(index).getAttributes().getNamedItem("package").getNodeValue();
				if (packageName.equals(Const.pkg)) {
					UUID uuid = UUID.randomUUID();
					String randomId = uuid.toString().replace("-", "").substring(0, 16);
					packages.item(index).getAttributes().getNamedItem("value").setNodeValue(randomId);
					packages.item(index).getAttributes().getNamedItem("defaultValue").setNodeValue(randomId);
					new FileUtils().writeFile(fileSSAID.getAbsolutePath(), new XmlUtils().toString(document), StandardCharsets.UTF_8);
					pussySSAID.commit();
					pussySSAID.setProperties(new int[]{600, 1000, 1000});
					return;
				}
			}
		}

		PussyFile pussySecure = new PussyFile("/data/system/users/" + PussyUser.getId() + "/settings_secure.xml");
		File fileSecure = pussySecure.getFile();
		String androidIdXml = new FileUtils().readFile(fileSecure.getAbsolutePath(), StandardCharsets.UTF_8);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(androidIdXml)));
		NodeList packages = document.getElementsByTagName("setting");
		for (int index = 0; index < packages.getLength(); index++) {
			for (int i = 0; i < packages.item(index).getAttributes().getLength(); i++) {
				String packageName = packages.item(index).getAttributes().getNamedItem("package").getNodeValue();
				String name = packages.item(index).getAttributes().getNamedItem("name").getNodeValue();
				if (packageName.equals("android") && name.equals("android_id")) {
					UUID uuid = UUID.randomUUID();
					String randomId = uuid.toString().replace("-", "").substring(0, 16);
					packages.item(index).getAttributes().getNamedItem("value").setNodeValue(randomId);
					new FileUtils().writeFile(fileSecure.getAbsolutePath(), new XmlUtils().toString(document), StandardCharsets.UTF_8);
					pussySecure.commit();
					pussySecure.setProperties(new int[]{600, 1000, 1000});
					return;
				}
			}
		}
	}
}
