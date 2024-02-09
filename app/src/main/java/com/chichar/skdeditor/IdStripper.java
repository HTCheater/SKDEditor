package com.chichar.skdeditor;

import com.chichar.skdeditor.gamefiles.IGameFile;
import com.chichar.skdeditor.utils.FileUtils;
import com.chichar.skdeditor.utils.XmlUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class IdStripper {
	public static String stripPrefs(File prefsFile)
			throws ParserConfigurationException, IOException, SAXException, JSONException {
		String prefs = FileUtils.readFile(prefsFile.getAbsolutePath(), StandardCharsets.UTF_8);
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
					name.equals("accountLoginRecords") ||
					name.contains("UsedAccounts") ||
					name.contains("unity.player_session") ||
					name.equals("unity.cloud_userid") ||
					name.contains("SdkStateCache")) {
				strings.item(index).getParentNode().removeChild(strings.item(index));
			}
		}
		prefs = XmlUtils.toString(document);
		return prefs;
	}

	public static byte[] stripSettings(File settingsFile, IGameFile gameFile)
			throws IOException, JSONException {
		byte[] settingsBytes = FileUtils.readAllBytes(settingsFile.getAbsolutePath());
		JSONObject settings = new JSONObject(gameFile.decrypt(settingsBytes));
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
		return gameFile.encrypt(settings.toString());
	}

	public static byte[] stripStatistics(File statisticFile, IGameFile gameFile)
			throws IOException {
		byte[] statisticsBytes = FileUtils.readAllBytes(statisticFile.getAbsolutePath());
		String statistics = gameFile.decrypt(statisticsBytes);
		statistics = statistics.replaceFirst(",[\\n\\r].*?\"fixGP_Test\":\\d+", "");
		return gameFile.encrypt(statistics);
	}
}
