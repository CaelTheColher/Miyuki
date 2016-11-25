package br.com.brjdevs.miyuki.lib;

import java.util.function.Function;
import java.util.regex.Matcher;

public class MatcherUtils {
	private static final Function<Matcher, CharSequence> MATCHER_TEXT = ReflectionEasyAsFuck.Virtual.getField(Matcher.class, "text", CharSequence.class);

	public static Function<Matcher, String> replaceAll(Function<String, String> replacement) {
		return matcher -> replaceAll(matcher, replacement);
	}

	public static String replaceAll(Matcher matcher, Function<String, String> replacement) {
		matcher.reset();
		boolean result = matcher.find();
		if (result) {
			StringBuffer sb = new StringBuffer();
			do {
				matcher.appendReplacement(sb, replacement.apply(matcher.group()));
				result = matcher.find();
			} while (result);
			matcher.appendTail(sb);
			return sb.toString();
		}
		return MATCHER_TEXT.apply(matcher).toString();
	}
}