package br.com.brjdevs.miyuki.utils;


import java.io.File;
import java.util.ArrayList;

public class Java {
	public static void restartApp() {
		try {
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = new File(Java.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (!currentJar.getName().endsWith(".jar")) throw new RuntimeException("Can't find jar!");
			final ArrayList<String> command = new ArrayList<>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
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
