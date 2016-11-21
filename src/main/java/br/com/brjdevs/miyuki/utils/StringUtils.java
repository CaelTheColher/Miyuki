package br.com.brjdevs.miyuki.utils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;

public class StringUtils {
	public static String[] splitArgs(String args, int expectedArgs) {
		String[] raw = args.split("\\s+", expectedArgs);
		if (expectedArgs < 1) return raw;
		return normalizeArray(raw, expectedArgs);
	}

	public static String[] normalizeArray(String[] raw, int expectedSize) {
		String[] normalized = new String[expectedSize];

		Arrays.fill(normalized, "");
		for (int i = 0; i < normalized.length; i++) {
			if (i < raw.length && raw[i] != null && !raw[i].isEmpty()) {
				normalized[i] = raw[i];
			}
		}
		return normalized;
	}

	public static String[] advancedSplitArgs(String args, int expectedArgs) {
		List<String> result = new ArrayList<>();
		boolean inAString = false;
		StringBuilder currentBlock = new StringBuilder();
		for (int i = 0; i < args.length(); i++) {
			if (args.charAt(i) == '"' && (i == 0 || args.charAt(i - 1) != '\\' || args.charAt(i - 2) == '\\')) //Entered a String Init/End
				inAString = !inAString;

			if (inAString) //We're at a String. Keep Going
				currentBlock.append(args.charAt(i));
			else if (Character.isSpaceChar(args.charAt(i))) //We found a Code Block
			{
				if (currentBlock.length() != 0) {
					if (currentBlock.charAt(0) == '"' && currentBlock.charAt(currentBlock.length() - 1) == '"') {
						currentBlock.deleteCharAt(0);
						currentBlock.deleteCharAt(currentBlock.length() - 1);
					}

					result.add(currentBlock.toString());
					currentBlock = new StringBuilder();
				}
			} else currentBlock.append(args.charAt(i));
		}

		if (currentBlock.length() != 0) {
			if (currentBlock.charAt(0) == '"' && currentBlock.charAt(currentBlock.length() - 1) == '"') {
				currentBlock.deleteCharAt(0);
				currentBlock.deleteCharAt(currentBlock.length() - 1);
			}

			result.add(currentBlock.toString());
		}

		String[] raw = result.toArray(new String[result.size()]);

		if (expectedArgs < 1) return raw;
		return normalizeArray(raw, expectedArgs);
	}

	public static Map<String, String> parse(String[] args) {
		Map<String, String> options = new HashMap<>();

		for (int i = 0; i < args.length; i++) {
			if (args[i].charAt(0) == '-' || args[i].charAt(0) == '/') //This start with - or /
			{
				args[i] = args[i].substring(1);
				if (i + 1 >= args.length || args[i + 1].charAt(0) == '-' || args[i + 1].charAt(0) == '/') //Next start with - (or last arg)
				{
					options.put(args[i], "null");
				} else {
					options.put(args[i], args[i + 1]);
					i++;
				}
			} else {
				options.put(null, args[i]);
			}
		}

		return options;
	}

	public static String notNullOrDefault(String str, String defaultStr) {
		if (str == null || str.trim().isEmpty()) return defaultStr;
		return str;
	}

	public static String limit(String value, int length) {
		StringBuilder buf = new StringBuilder(value);
		if (buf.length() > length) {
			buf.setLength(length - 3);
			buf.append("...");
		}

		return buf.toString();
	}

	public static String removeLines(String str, int startline, int numlines) {
		try (BufferedReader br = new BufferedReader(new StringReader(str))) {
			//String buffer to store contents of the file
			StringBuilder builder = new StringBuilder("");

			//Keep track of the line number
			int linenumber = 1;
			numlines--;
			String line;

			while ((line = br.readLine()) != null) {
				//Store each valid line in the string buffer
				if (linenumber < startline || linenumber >= startline + numlines)
					builder.append(line).append("\n");
				linenumber++;
			}

			return builder.toString();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
