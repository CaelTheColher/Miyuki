package br.com.brjdevs.miyuki.utils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;

public class PatternCollection {
	public static final Pattern
		HTML_TO_PLAIN = Pattern.compile("(?s)<[^>]*>(\\s*<[^>]*>)*"),
		MULTIPLE_LINES = Pattern.compile("\"(?m)^[ \\t]*\\r?\\n\""),
		UNNECESSARY_NEWLINE_END = Pattern.compile("(\\r?\\n)+$"),
		UNNECESSARY_NEWLINE_START = Pattern.compile("^(\\r?\\n)+");

	public static Pattern compileForHTMLTag(String tag) {
		tag = quote(tag);
		return Pattern.compile("<\\/?" + tag + "[^>]*>");
	}

	public static Pattern compileForHTMLContents(String tag) {
		tag = quote(tag);
		return Pattern.compile("<" + tag + "[^>]*>[\\S\\s]+?<\\/" + tag + ">");
	}

	public static Function<String, String> compileReplace(Pattern pattern, String replace) {
		return s -> pattern.matcher(s).replaceAll(replace);
	}

	public static Function<String, String> compileReplace(Pattern pattern, Function<Matcher, String> replace) {
		return s -> replace.apply(pattern.matcher(s));
	}
}
