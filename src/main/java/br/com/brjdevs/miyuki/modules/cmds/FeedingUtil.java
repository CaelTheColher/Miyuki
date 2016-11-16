package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.java.lib.IOHelper;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.utils.EncodingUtil;
import br.com.brjdevs.miyuki.utils.HTML2Discord;
import br.com.brjdevs.miyuki.utils.PatternCollection;
import br.com.brjdevs.miyuki.utils.StringUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import net.dv8tion.jda.core.entities.TextChannel;

import java.net.URL;
import java.util.function.Function;

import static br.com.brjdevs.miyuki.utils.PatternCollection.compileReplace;

@SuppressWarnings("ConstantConditions")
public class FeedingUtil {

	public static String shorten(String url) {
		return IOHelper.toString("https://is.gd/create.php?format=simple&url=" + EncodingUtil.encodeURIComponent(url));
	}

	public static String shorten(String url, String shorturl) {
		System.out.println("https://is.gd/create.php?format=simple&url=" + EncodingUtil.encodeURIComponent(url) + "&shorturl=" + shorturl);
		return IOHelper.toString("https://is.gd/create.php?format=simple&url=" + EncodingUtil.encodeURIComponent(url) + "&shorturl=" + shorturl);
	}

	public static URL shorten(URL url) {
		return IOHelper.newURL(shorten(url.toString()));
	}

	public static URL shorten(URL url, String shorturl) {
		return IOHelper.newURL(shorten(url.toString(), shorturl));
	}

	public static Function<TextChannel, String> handleEntry(final FeedCmd.Subscription subscription, final SyndEntry feed) {
		//Compile static things
		Function<TextChannel, String> chunk2, chunk4, chunk6, chunk7;
		String chunk1 = "***:envelope_with_arrow: - ";
		String chunk3 = " (:bell:: `" + subscription.pushName + "` -" + subscription.url.toString() + ")***"
			+ (feed.getDescription() != null ? "\n" + StringUtils.limit(HTML2Discord.toDiscordFormat(feed.getDescription().getValue()), 750) : "") +
			"\n***:envelope:: ";
		String chunk5 = " (";
		String chunk8 = ")***";

		if (feed.getTitle() != null) {
			String titleStatic = StringUtils.limit(compileReplace(PatternCollection.MULTIPLE_LINES, "\n").apply(HTML2Discord.toPlainText(feed.getTitle())), 70);
			chunk2 = c -> titleStatic;
		} else {
			chunk2 = c -> I18nModule.getLocalized("feed.untitled", c);
		}

		if (feed.getLink() != null) {
			String linkStatic = shorten(HTML2Discord.toPlainText(feed.getLink()));
			chunk4 = c -> linkStatic;
		} else {
			chunk4 = c -> I18nModule.getLocalized("feed.unknown", c);
		}

		chunk6 = c -> I18nModule.getLocalized("feed.at", c);

		if (feed.getPublishedDate() != null) {
			String dateStatic = feed.getPublishedDate().toString();
			chunk7 = c -> dateStatic;
		} else {
			chunk7 = c -> I18nModule.getLocalized("feed.unknown", c);
		}

		return channel -> chunk1 + chunk2.apply(channel) + chunk3 + chunk4.apply(channel) + chunk5 + chunk6.apply(channel) + " " + chunk7.apply(channel) + chunk8;
	}
}
