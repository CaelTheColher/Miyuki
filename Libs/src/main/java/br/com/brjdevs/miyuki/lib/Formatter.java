package br.com.brjdevs.miyuki.lib;

public class Formatter {
	public static String encase(String content) {
		return encase(content, "");
	}

	public static String encase(String content, String language) {
		return "```" + language + "\n" + content + "\n```";
	}

	public static String italic(String content) {
		return "*" + content + "*";
	}

	public static String bold(String content) {
		return italic(italic(content));
	}

	public static String boldAndItalic(String content) {
		return bold(italic(content));
	}
}
