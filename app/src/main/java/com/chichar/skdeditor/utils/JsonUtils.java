package com.chichar.skdeditor.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

	public static String prettyPrintJSON(String unformattedJsonString) {
		StringBuilder prettyJSONBuilder = new StringBuilder();
		int indentLevel = 0;
		boolean inQuote = false;
		char[] charArray = unformattedJsonString.toCharArray();
		for (char charFromUnformattedJson : charArray) {
			switch (charFromUnformattedJson) {
				case ' ':
					if (inQuote) {
						prettyJSONBuilder.append(charFromUnformattedJson);
					}
					break;
				case '\"':
					inQuote = !inQuote;
					prettyJSONBuilder.append(charFromUnformattedJson);
					break;
				case ',':
					prettyJSONBuilder.append(charFromUnformattedJson);
					if (!inQuote) {
						appendIndentedNewLine(indentLevel, prettyJSONBuilder);
					}
					break;
				case '[':
				case '{':
					prettyJSONBuilder.append(charFromUnformattedJson);
					indentLevel++;
					appendIndentedNewLine(indentLevel, prettyJSONBuilder);
					break;
				case ']':
				case '}':
					indentLevel--;
					appendIndentedNewLine(indentLevel, prettyJSONBuilder);
					prettyJSONBuilder.append(charFromUnformattedJson);
					break;
				default:
					prettyJSONBuilder.append(charFromUnformattedJson);
					break;
			}
		}
		return prettyJSONBuilder.toString();
	}

	private static void appendIndentedNewLine(int indentLevel, StringBuilder stringBuilder) {
		stringBuilder.append("\n");
		for (int i = 0; i < indentLevel; i++) {
			stringBuilder.append("  ");
		}
	}

	public static String minify(String string) {
		try {
			// may break if string is array
			JSONObject json = new JSONObject(string);
			return json.toString();
		} catch (JSONException e) {
			// String is not a JSON?
			return string;
		}
	}
}