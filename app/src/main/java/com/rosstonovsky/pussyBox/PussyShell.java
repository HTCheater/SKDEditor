package com.rosstonovsky.pussyBox;


import android.util.Log;

import androidx.annotation.NonNull;

import com.chichar.skdeditor.CrashHandler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PussyShell {

	private List<String> stdout = new ArrayList<>();

	private List<String> stderr = new ArrayList<>();

	private static NoCloseInputStream in;
	private static NoCloseOutputStream out;
	private static NoCloseInputStream err;
	private static Runnable onError;
	private final String uuid = UUID.randomUUID().toString();

	private String commands;

	private static String toyboxPath = "." + PussyUser.getAppFilesFolder() + "/bin/toybox ";

	public static String getToyboxPath() {
		return toyboxPath;
	}

	public static void setToyboxPath(String toyboxPath) {
		PussyShell.toyboxPath = toyboxPath;
	}

	public PussyShell cmd(String commands) {
		this.commands = commands;
		return this;
	}

	public static void init(Runnable r) throws IOException {
		Process process = Runtime.getRuntime().exec("su");
		in = new NoCloseInputStream(process.getInputStream());
		out = new NoCloseOutputStream(process.getOutputStream());
		err = new NoCloseInputStream(process.getErrorStream());
		onError = r;
	}

	public static boolean isRoot() {
		Executor executor = Executors.newSingleThreadExecutor();
		AtomicBoolean isRoot = new AtomicBoolean(false);
		executor.execute(() -> {
			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(in, StandardCharsets.UTF_8));
			try {
				out.write("id\n".getBytes(StandardCharsets.UTF_8));
				out.flush();
				String s = stdInput.readLine();
				if (s.contains("uid=0")) {
					isRoot.set(true);
				}
			} catch (IOException ignored) {
			}
		});
		for (int i = 0; i < 3000; i++) {
			if (isRoot.get()) {
				return true;
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException ignored) {
			}
		}
		return isRoot.get();
	}

	public List<String> run(String command) {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		cmd(command).to(stdout, stderr).exec();
		if (stderr.size() == 0) {
			return stdout;
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("There was an error while executing command:\n");
		stringBuilder.append(command);
		for (int i = 0; i < stderr.size(); i++) {
			stringBuilder.append("\n");
			stringBuilder.append(stderr.get(i));
		}

		new CrashHandler().writeToFile(stringBuilder.toString());
		onError.run();
		return stdout;
	}

	public List<String> toybox(String command) {
		return run(toyboxPath + command);
	}

	public static void closeStreams() {
		try {
			in.Close();
			out.Close();
			err.Close();
		} catch (IOException ignored) {
		}
	}

	public void exec() {
		try {
			Log.i("PussyShell", "Command: " + commands);
			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(in, StandardCharsets.UTF_8));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(err, StandardCharsets.UTF_8));
			out.write((commands + String.format(";echo %s;>&2 echo %s", uuid, uuid) + "\n").getBytes(StandardCharsets.UTF_8));
			out.flush();

			String s;
			while (validLine(s = stdInput.readLine())) {
				stdout.add(s);
				Log.i("PussyShell", "stdout: " + s);
			}

			while (validLine(s = stdError.readLine())) {
				stderr.add(s);
				Log.i("PussyShell", "stderr: " + s);
			}
			stdInput.close();
			stdError.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean validLine(String line) {
		if (line == null) {
			return false;
		}
		return !line.equals(uuid);
	}

	public PussyShell to(List<String> stdout, List<String> stderr) {
		this.stdout = stdout;
		this.stderr = stderr;
		return this;
	}

	public PussyShell to(List<String> stdout) {
		this.stdout = stdout;
		return this;
	}

	private static class NoCloseInputStream extends FilterInputStream {

		NoCloseInputStream(InputStream in) {
			super(in);
		}

		@Override
		public void close() {
		}

		void Close() throws IOException {
			in.close();
		}
	}

	private static class NoCloseOutputStream extends FilterOutputStream {

		NoCloseOutputStream(@NonNull OutputStream out) {
			super((out instanceof BufferedOutputStream) ? out : new BufferedOutputStream(out));
		}

		@Override
		public void write(@NonNull byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			out.flush();
		}

		void Close() throws IOException {
			super.close();
		}
	}
}
