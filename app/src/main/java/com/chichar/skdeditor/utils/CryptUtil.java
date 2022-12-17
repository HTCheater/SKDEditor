package com.chichar.skdeditor.utils;

import android.util.Base64;
import android.util.Log;

import com.chichar.skdeditor.Const;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptUtil {

	public byte[] encrypt(String data, String fileName) {
		byte[] result = data.getBytes(StandardCharsets.UTF_8);
		if (fileName.equals("battles.data") ||
				fileName.equals("battle.data") ||
				Const.encryptedJsonGameFiles.contains(fileName)) {
			result = new JsonUtils().minify(data).getBytes(StandardCharsets.UTF_8);
		}
		if (fileName.contains("item_data") ||
				fileName.equals("task.data") ||
				fileName.equals("setting.data") ||
				fileName.contains("season_data")) {
			result = encryptDES(data, new byte[]{0x69, 0x61, 0x6d, 0x62, 0x6f, 0x0, 0x0, 0x0});
		}
		if (fileName.equals("statistic.data")) {
			result = encryptDES(data, new byte[]{0x63, 0x72, 0x73, 0x74, 0x31, 0x0, 0x0, 0x0});
		}
		if (fileName.equals("game.data")) {
			result = xor(data.getBytes(StandardCharsets.UTF_8));
		}
		return result;
	}

	public String decrypt(byte[] data, String fileName) {
		Log.d("TAG", fileName);
		String result = new String(data);
		if (fileName.contains(".xml")) {
			Pattern attrsRegex = Pattern.compile("(<.*?/>|<.*?</.*?>)");
			Matcher attrsMatcher = attrsRegex.matcher(new String(data));
			ArrayList<String> attrs = new ArrayList<>();

			StringBuilder finalxml = new StringBuilder();
			finalxml.append("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>");

			while (attrsMatcher.find()) {
				for (int i = 0; i < attrsMatcher.groupCount(); i++) {
					attrs.add(attrsMatcher.group(i));
				}
			}

			Collections.sort(attrs, String::compareToIgnoreCase);
			String[] attrsArr = attrs.toArray(new String[0]);
			for (String attr : attrsArr) {
				finalxml.append("\n    ");
				finalxml.append(attr);
			}
			finalxml.append("\n</map>");
			result = finalxml.toString();
		}

		if (fileName.contains("item_data") ||
				fileName.equals("task.data") ||
				fileName.equals("setting.data") ||
				fileName.contains("season_data")) {
			result = decryptDES(data, new byte[]{0x69, 0x61, 0x6d, 0x62, 0x6f, 0x0, 0x0, 0x0}); //iambo
		}

		if (fileName.equals("statistic.data")) {
			result = decryptDES(data, new byte[]{0x63, 0x72, 0x73, 0x74, 0x31, 0x0, 0x0, 0x0}); //crst1
		}

		if (fileName.equals("game.data")) {
			result = new String(xor(data));
		}
		if (fileName.equals("battles.data") ||
				fileName.equals("battle.data") ||
				Const.encryptedJsonGameFiles.contains(fileName)) {
			JsonUtils jsonUtils = new JsonUtils();
			result = jsonUtils.prettyPrintJSON(jsonUtils.minify(result));
		}
		return result;
	}

	private byte[] xor(byte[] data) {
		byte[] key = new byte[15];
		key[0] = 115;
		key[1] = 108;
		key[2] = 99;
		key[3] = 122;
		key[4] = 125;
		key[5] = 103;
		key[6] = 117;
		key[7] = 99;
		key[8] = 127;
		key[9] = 87;
		key[10] = 109;
		key[11] = 108;
		key[12] = 107;
		key[13] = 74;
		key[14] = 95;

		byte[] output = new byte[data.length];

		for (int i = 0; i < data.length; i++) {
			output[i] = (byte) (key[i % 15] ^ data[i]);
		}

		return output;
	}

	private String decryptDES(byte[] text, byte[] key) {
		byte[] iv = new byte[8];
		iv[0] = 0x41;
		iv[1] = 0x68;
		iv[2] = 0x62;
		iv[3] = 0x6f;
		iv[4] = 0x6f;
		iv[5] = 0x6c;
		iv[6] = 0x0;
		iv[7] = 0x0;

		byte[] cypherBytes = Base64.decode(text, Base64.NO_WRAP);
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		try {
			assert cipher != null;
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
		} catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
			e.printStackTrace();
		}

		byte[] resultBytes = new byte[0];
		try {
			resultBytes = cipher.doFinal(cypherBytes);
		} catch (BadPaddingException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		return new String(resultBytes);
	}

	private byte[] encryptDES(String text, byte[] key) {
		byte[] iv = new byte[8];
		iv[0] = 0x41;
		iv[1] = 0x68;
		iv[2] = 0x62;
		iv[3] = 0x6f;
		iv[4] = 0x6f;
		iv[5] = 0x6c;
		iv[6] = 0x0;
		iv[7] = 0x0;

		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DES");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		try {
			assert cipher != null;
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
		} catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
			e.printStackTrace();
		}

		byte[] resultBytes = new byte[0];
		try {
			resultBytes = cipher.doFinal(text.getBytes());
		} catch (BadPaddingException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		return Base64.encode(resultBytes, Base64.NO_WRAP);
	}
}
