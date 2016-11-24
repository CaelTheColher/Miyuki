package br.com.brjdevs.miyuki.utils;

import br.com.brjdevs.miyuki.utils.log.SimpleLogToSLF4JAdapter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.concurrent.CompletableFuture;

public class DiscordUtils {
	public static String guessGuildLanguage(Guild guild) {
		switch (guild.getRegion()) {
			case BRAZIL:
			case VIP_BRAZIL:
				return "pt_BR";
			case AMSTERDAM:
			case EU_WEST:
			case EU_CENTRAL:
			case FRANKFURT:
			case LONDON:
			case VIP_AMSTERDAM:
			case VIP_EU_WEST:
			case VIP_EU_CENTRAL:
			case VIP_FRANKFURT:
			case VIP_LONDON:
				return "en_GB";
			case SINGAPORE:
			case VIP_SINGAPORE:
				return "en_SG";
			case SYDNEY:
			case VIP_SYDNEY:
				return "en_AU";
			case US_EAST:
			case US_WEST:
			case US_CENTRAL:
			case US_SOUTH:
			case VIP_US_EAST:
			case VIP_US_WEST:
			case VIP_US_CENTRAL:
			case VIP_US_SOUTH:
			case UNKNOWN:
			default:
				return "en_US";
		}
	}

	public static String name(User user, Guild guild) {
		return guild != null && guild.getMember(user) != null && guild.getMember(user).getNickname() != null ? guild.getMember(user).getNickname() : user.getName();
	}

	public static String processId(String string) {
		if (string.startsWith("<@") && string.endsWith(">")) string = string.substring(2, string.length() - 1);
		if (string.startsWith("!")) string = string.substring(1);
		return string.toLowerCase();
	}

	public static void hackJDALog() {
		SimpleLog.addListener(new SimpleLogToSLF4JAdapter());
		SimpleLog.LEVEL = SimpleLog.Level.OFF;
	}

	public static <T> CompletableFuture<T> submit(RestAction<T> restAction) {
		CompletableFuture<T> future = new CompletableFuture<T>();
		restAction.queue(future::complete, future::completeExceptionally);
		return future;
	}
}
