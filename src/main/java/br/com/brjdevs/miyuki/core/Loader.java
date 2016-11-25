package br.com.brjdevs.miyuki.core;

import br.com.brjdevs.java.lib.IOHelper;
import br.com.brjdevs.miyuki.lib.DiscordUtils;
import br.com.brjdevs.miyuki.lib.Java;
import br.com.brjdevs.miyuki.modules.db.DBModule;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.reflections.Reflections;
import org.slf4j.Logger;

import static br.com.brjdevs.miyuki.lib.log.LogUtils.logger;

public class Loader {
	public static final Logger LOGGER = logger("Loader");
	public static String[] args;


	public static void main(String[] args) throws Exception {
		Loader.args = args;
		DiscordUtils.hackJDALog();

		try {
			new Reflections("br.com.brjdevs.miyuki.modules").getTypesAnnotatedWith(Module.class).forEach(c -> {
				try {
					ModuleManager.add(c);
				} catch (Exception e) {
					LOGGER.error("Failed to load Module from " + c, e);
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
