package com.chichar.skdeditor.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

public class FileUtils {
	
	public String readFile(String path, Charset charset) {
		String everything = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(path), String.valueOf(charset)))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return everything;
	}
	
	public void writeFile(String path, String text, Charset charset) {
		try (OutputStreamWriter writer =
				     new OutputStreamWriter(new FileOutputStream(path), charset)) {
			writer.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] readAllBytes(String path) throws IOException {
		RandomAccessFile f = new RandomAccessFile(path, "r");
		byte[] b = new byte[(int)f.length()];
		f.readFully(b);
		return b;
	}

	public void copy(File src, File dst) throws IOException {
		try (InputStream in = new FileInputStream(src)) {
			try (OutputStream out = new FileOutputStream(dst)) {
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
		}
	}

	public void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
		in.close();
		out.close();
	}

	public void writeBytes(File file, byte[] bytes) {
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(bytes);
		} catch (IOException e) {
			Log.d("TAG", "writeBytes: " + e.toString());
		}
	}
}
