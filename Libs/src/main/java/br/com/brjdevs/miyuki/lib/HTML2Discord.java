package br.com.brjdevs.miyuki.lib;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.XML;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTML2Discord {
	public static final List<Function<String, String>> FUNCTION_LIST = Arrays.asList(
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("blockquote"), ""),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("br"), "\n"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("b"), "**"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("em"), "*"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("strike"), "~~"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("u"), "__"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("kbd"), "`"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("code"), "`"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("pre"), "```"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("p"), "\n"),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLTag("img"), MatcherUtils.replaceAll(s -> {
			JsonElement element = new JsonParser().parse(XML.toJSONObject(s).toString());
			if (!element.isJsonObject() || !element.getAsJsonObject().get("img").isJsonObject()) return "";
			JsonObject object = element.getAsJsonObject().get("img").getAsJsonObject();
			if (!object.has("src")) return "(no image)";
			return object.get("src").getAsString();
		})),
		PatternCollection.compileReplace(PatternCollection.compileForHTMLContents("a"), MatcherUtils.replaceAll(s -> {
			String inside = s.substring(s.indexOf('>') + 1, s.lastIndexOf('<'));
			s = s.substring(0, s.indexOf('>') + 1) + "nvm" + s.substring(s.lastIndexOf('<'));
			JsonElement element = new JsonParser().parse(XML.toJSONObject(s).toString());
			if (!element.isJsonObject() || !element.getAsJsonObject().get("a").isJsonObject()) return inside;
			JsonObject object = element.getAsJsonObject().get("a").getAsJsonObject();
			if (!object.has("href")) return inside;
			return inside + " (" + object.get("href").getAsString() + ")";
		})),
		PatternCollection.compileReplace(Pattern.compile("````"), "```"),
		PatternCollection.compileReplace(PatternCollection.HTML_TO_PLAIN, ""),
		PatternCollection.compileReplace(Pattern.compile(":(?=[*_~`])"), ": "),
		PatternCollection.compileReplace(Pattern.compile("[*_~`]\\s+?"), ""),
		PatternCollection.compileReplace(PatternCollection.UNNECESSARY_NEWLINE_END, ""),
		PatternCollection.compileReplace(PatternCollection.UNNECESSARY_NEWLINE_START, ""),
		PatternCollection.compileReplace(PatternCollection.MULTIPLE_LINES, "\n")
	);

	public static String toDiscordFormat(String html) {
		Holder<String> h = new Holder<>(html);
		FUNCTION_LIST.forEach(f -> h.var = f.apply(h.var));
		return h.var;
	}

	public static String toPlainText(String discordFormatMessage) {
		String strippedContent;
		//all the formatting keys to keep track of
		String[] keys = new String[]{"*", "_", "`", "~~"};

		//find all tokens (formatting strings described above)
		TreeSet<FormatToken> tokens = new TreeSet<>((t1, t2) -> Integer.compare(t1.start, t2.start));
		for (String key : keys) {
			Matcher matcher = Pattern.compile(Pattern.quote(key)).matcher(discordFormatMessage);
			while (matcher.find()) {
				tokens.add(new FormatToken(key, matcher.start()));
			}
		}

		//iterate over all tokens, find all matching pairs, and add them to the list toRemove
		Stack<FormatToken> stack = new Stack<>();
		List<FormatToken> toRemove = new ArrayList<>();
		boolean inBlock = false;
		for (FormatToken token : tokens) {
			if (stack.empty() || !stack.peek().format.equals(token.format) || stack.peek().start + token.format.length() == token.start) {
				//we are at opening tag
				if (!inBlock) {
					//we are outside of block -> handle normally
					if ("`".equals(token.format)) {
						//block start... invalidate all previous tags
						stack.clear();
						inBlock = true;
					}
					stack.push(token);
				} else if ("`".equals(token.format)) {
					//we are inside of a block -> handle only block tag
					stack.push(token);
				}
			} else if (!stack.empty()) {
				//we found a matching close-tag
				toRemove.add(stack.pop());
				toRemove.add(token);
				if ("`".equals(token.format) && stack.empty()) {
					//close tag closed the block
					inBlock = false;
				}
			}
		}

		//sort tags to remove by their start-index and iteratively build the remaining string
		Collections.sort(toRemove, (t1, t2) -> Integer.compare(t1.start, t2.start));
		StringBuilder out = new StringBuilder();
		int currIndex = 0;
		for (FormatToken formatToken : toRemove) {
			if (currIndex < formatToken.start) {
				out.append(discordFormatMessage.substring(currIndex, formatToken.start));
			}
			currIndex = formatToken.start + formatToken.format.length();
		}
		if (currIndex < discordFormatMessage.length()) {
			out.append(discordFormatMessage.substring(currIndex));
		}
		//return the stripped text, escape all remaining formatting characters (did not have matching open/close before or were left/right of block
		strippedContent = out.toString().replace("*", "\\*").replace("_", "\\_").replace("~", "\\~");
		return strippedContent;
	}

	private static class FormatToken {
		public final String format;
		public final int start;

		public FormatToken(String format, int start) {
			this.format = format;
			this.start = start;
		}
	}
}
