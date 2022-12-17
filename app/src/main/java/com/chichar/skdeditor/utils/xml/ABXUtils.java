package com.chichar.skdeditor.utils.xml;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ABXUtils {
	//I completely have no idea if it is gonna work on all devices
	//Upd.: I have no idea if it actually works
	private byte[] src;
	private final ArrayList<String> strings = new ArrayList<>();

	public void setSource(byte[] src) {
		this.src = src;
		getStrings();
	}

	private void getStrings() {
		int arrIndex = 4; //skip header

		while (arrIndex < src.length) {
			if (src[arrIndex] == (byte) 0xff && src[arrIndex + 1] == (byte) 0xff && src[arrIndex + 2] == 0x00) {
				arrIndex++;
				arrIndex++;
				arrIndex++;
				int strLen = src[arrIndex];
				byte[] str = new byte[strLen];
				arrIndex++;
				for (int i = 0; i < strLen; i++, arrIndex++) {
					str[i] = src[arrIndex];
				}
				Log.d("TAG", "getStrings: " + new String(str));
				strings.add(new String(str));
			}
			arrIndex++;
		}
	}


	/**
	 * returns "value" attribute of element specified by other attribute name and its value
	 */
	public String getStringAttributeValue(String attr, String attrValue) throws Exception {
		int attrIndex = strings.indexOf(attr);
		if (attrIndex == -1) {
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < strings.size(); i++) {
				sb.append("\nName: ").append(strings.get(i)).append(", index: ").append(i);
			}
			throw new Exception("Failed to process ABX file:\nAttribute \"" + attr + "\" not found\nDebug:" + sb);
		}

		int arrIndex = 4; //skip header
		boolean isReadingAttr = false;
		HashMap<String, String> currAttrs = new HashMap<>();


		while (arrIndex < src.length) {
			if (src[arrIndex] == 0x32 && src[arrIndex + 1] == 0x00) {
				arrIndex++;
				arrIndex++;
				currAttrs = new HashMap<>();
				break;
			}
			arrIndex++;
		}

		while (arrIndex < src.length) {
			if (src[arrIndex] == 0x32 && src[arrIndex + 1] == 0x00) {
				arrIndex++;
				arrIndex++;
				currAttrs = new HashMap<>();
				isReadingAttr = true;
			}
			do {
				if (src[arrIndex] == 0x33 && src[arrIndex + 1] == 0x00) {
					isReadingAttr = false;
					Log.d("TAG", "tag closed index: " + arrIndex);
					Log.d("TAG", "attr value: " + currAttrs.get(attr));
					if (currAttrs.get(attr) != null && Objects.equals(currAttrs.get(attr), attrValue)) {
						return currAttrs.get("value");
					}
					arrIndex++;
					arrIndex++;
					arrIndex++;
					break;
				} else if (src[arrIndex] == (byte) 0x2F && src[arrIndex + 1] == 0x00) {
					isReadingAttr = true;
					arrIndex++;
					arrIndex++;
					break;
				} else {
					arrIndex++;
				}
			} while ((src[arrIndex - 1] != (byte) 0x2F && src[arrIndex] != 0x00) ||
					(src[arrIndex - 1] != 0x33 && src[arrIndex] != 0x00));
			if (isReadingAttr) {
				int stringIndex = src[arrIndex];
				arrIndex++;
				arrIndex++;
				int strLen = src[arrIndex];
				arrIndex++;
				Log.d("TAG", "strLen: " + strLen);
				byte[] str = new byte[strLen];
				for (int i = 0; i < strLen; i++, arrIndex++) {
					str[i] = src[arrIndex];
				}
				arrIndex--;
				Log.d("TAG", "HashMap key: " + strings.get(stringIndex) + ", value: " + new String(str));
				currAttrs.put(strings.get(stringIndex), new String(str));
				isReadingAttr = false;
			}

			if (src[arrIndex] == 0x33 && src[arrIndex + 1] == 0x00) {
				arrIndex++;
				arrIndex++;
				Log.d("TAG", "attr value: " + currAttrs.get(attr));
				if (currAttrs.get(attr) != null && Objects.equals(currAttrs.get(attr), attrValue)) {
					return currAttrs.get("value");
				}
				currAttrs = new HashMap<>();
			}
			arrIndex++;
		}
		return "Failed to process ABX file";
	}

	//modifies "value" attribute of element specified by other attribute name and it's value
	public byte[] setStringtAtributeValue(String attr, String attrValue, byte[] replacement) throws Exception {
		int attrIndex = strings.indexOf(attr);
		if (attrIndex == -1) {
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < strings.size(); i++) {
				sb.append("\nName: ").append(strings.get(i)).append(", index: ").append(i);
			}
			throw new Exception("Failed to process ABX file:\nAttribute \"" + attr + "\" not found\nDebug:" + sb);
		}

		int arrIndex = 0;
		boolean isReadingAttr = false;
		HashMap<String, String> currAttrs = new HashMap<>();
		HashMap<String, Integer> attrsStart = new HashMap<>();
		HashMap<String, Integer> attrsLength = new HashMap<>();


		while (arrIndex < src.length) {
			if (src[arrIndex] == 0x32 && src[arrIndex + 1] == 0x00) {
				arrIndex++;
				arrIndex++;
				currAttrs = new HashMap<>();
				attrsStart = new HashMap<>();
				attrsLength = new HashMap<>();
				break;
			}
			arrIndex++;
		}

		while (arrIndex < src.length) {
			if (src[arrIndex] == 0x32 && src[arrIndex + 1] == 0x00) {
				arrIndex++;
				arrIndex++;
				currAttrs = new HashMap<>();
				attrsStart = new HashMap<>();
				attrsLength = new HashMap<>();
				isReadingAttr = true;
			}
			do {
				if (src[arrIndex] == 0x33 && src[arrIndex + 1] == 0x00) {
					isReadingAttr = false;
					Log.d("TAG", "tag closed index: " + arrIndex);
					arrIndex++;
					arrIndex++;
					arrIndex++;
					break;
				} else if (src[arrIndex] == (byte) 0x2F && src[arrIndex + 1] == 0x00) {
					isReadingAttr = true;
					arrIndex++;
					arrIndex++;
					break;
				} else {
					arrIndex++;
				}
			} while ((src[arrIndex - 1] != (byte) 0x2F && src[arrIndex] != 0x00) ||
					(src[arrIndex - 1] != 0x33 && src[arrIndex] != 0x00));
			if (isReadingAttr) {
				int stringIndex = src[arrIndex];
				arrIndex++;
				arrIndex++;
				int strLen = src[arrIndex];
				arrIndex++;
				int startIndex = arrIndex;
				Log.d("TAG", "strLen: " + strLen);
				byte[] str = new byte[strLen];
				for (int i = 0; i < strLen; i++, arrIndex++) {
					str[i] = src[arrIndex];
				}
				arrIndex--;
				Log.d("TAG", "HashMap key: " + strings.get(stringIndex) + ", value: " + new String(str));
				currAttrs.put(strings.get(stringIndex), new String(str));
				attrsStart.put(strings.get(stringIndex), startIndex);
				attrsLength.put(strings.get(stringIndex), strLen);
				isReadingAttr = false;
			}

			if (src[arrIndex] == 0x33 && src[arrIndex + 1] == 0x00) {
				arrIndex++;
				arrIndex++;
				if (currAttrs.get(attr) != null && Objects.equals(currAttrs.get(attr), attrValue)) {
					Integer startIndex = attrsStart.get("value");
					Integer length = attrsLength.get("value");
					if (!Objects.equals(startIndex, null) && !Objects.equals(length, null)) {
						for (int i = 0; i < length; i++) {
							if (i >= replacement.length) {
								//I don't need to change size of value since the size is always 16
								throw new Exception("Replacement must be the same size as attribute value");
							}
							src[startIndex + i] = replacement[i];
						}
						startIndex = attrsStart.get("defaultValue");
						length = attrsLength.get("defaultValue");
						if (!Objects.equals(startIndex, null) && !Objects.equals(length, null)) {
							for (int i = 0; i < length; i++) {
								if (i >= replacement.length) {
									throw new Exception("Replacement must be the same size as attribute value");
								}
								src[startIndex + i] = replacement[i];
							}
							return src;
						}
						return src;
					}
					throw new Exception("Start index or length of the attribute is null");
				}
			}
			arrIndex++;
		}
		return src;
	}
}
