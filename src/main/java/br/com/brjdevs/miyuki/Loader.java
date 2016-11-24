package br.com.brjdevs.miyuki;

import br.com.brjdevs.java.lib.IOHelper;
import br.com.brjdevs.miyuki.loader.ModuleManager;
import br.com.brjdevs.miyuki.modules.db.DBModule;
import br.com.brjdevs.miyuki.utils.DiscordUtils;
import br.com.brjdevs.miyuki.utils.Java;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.slf4j.Logger;

import java.util.stream.Stream;

import static br.com.brjdevs.miyuki.utils.log.LogUtils.logger;

public class Loader {
	public static final Logger LOGGER = logger("Loader");
	public static String[] args;


	public static void main(String[] args) throws Exception {
		Loader.args = args;
		DiscordUtils.hackJDALog();

		try {
			Stream.of(resource(Loader.class, "/modules.miyuki").split("\\r?\\n"))
				.map(String::trim)
				.filter(s -> !s.startsWith("#"))
				.forEach(s -> {
					try {
						ModuleManager.add(Class.forName(s));
					} catch (Exception e) {
						LOGGER.error("Failed to load Module " + s, e);
					}
				});


			ModuleManager.firePreReadyEvents();

			new JDABuilder(AccountType.BOT)
				.setToken(DBModule.getConfig().get("token").getAsString())
				.setEventManager(new AnnotatedEventManager())
				.addListener(ModuleManager.jdaListeners())
				.buildBlocking();

			ModuleManager.firePostReadyEvents();
		} catch (Exception e) {
			e.printStackTrace();
			Java.stopApp();
		}
	}

	public static String resource(Class c, String file) {
		return IOHelper.toString(c.getResourceAsStream(file));
	}
}
