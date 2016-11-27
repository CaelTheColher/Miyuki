package br.com.brjdevs.miyuki.lib.core;


import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Java {
	public static Path getPath(String file, String ext) {
		return getPath(file + "." + ext);
	}

	public static Path getPath(String file) {
		try {
			return Paths.get(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void restartApp() {
		try {
			if (SystemUtils.IS_OS_LINUX && getPath("start", "sh").toFile().exists()) {
				Runtime.getRuntime().exec("bash " + getPath("start", "sh").toFile().getAbsolutePath());
				stopApp();
				return;
			}

			if (SystemUtils.IS_OS_WINDOWS && getPath("start", "bat").toFile().exists()) {
				Runtime.getRuntime().exec("cmd /c start " + getPath("start", "bat").toFile().getAbsolutePath());
				stopApp();
				return;
			}

			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = new File(Java.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (!currentJar.getName().endsWith(".jar")) throw new RuntimeException("Can't find jar!");
			final ArrayList<String> command = new ArrayList<>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			new ProcessBuilder(command).start();
			stopApp();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void stopApp() {
		System.exit(0);
	}
}
