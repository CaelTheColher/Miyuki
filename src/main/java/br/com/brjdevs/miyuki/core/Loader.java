package br.com.brjdevs.miyuki.core;

import br.com.brjdevs.java.lib.IOHelper;
import br.com.brjdevs.miyuki.lib.DiscordUtils;
import br.com.brjdevs.miyuki.lib.Java;
import br.com.brjdevs.miyuki.modules.db.DBModule;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.slf4j.Logger;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import static br.com.brjdevs.miyuki.lib.log.LogUtils.logger;

public class Loader {
	public static final Logger LOGGER = logger("Loader");
	public static String[] args;


	public static void main(String[] args) throws Exception {
		Loader.args = args;
		DiscordUtils.hackJDALog();

		try {
			ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(false);

			scanner.addIncludeFilter(new AnnotationTypeFilter(Module.class));

			scanner.findCandidateComponents("br.com.brjdevs.miyuki.core.modules").forEach(bean -> {
				try {
					ModuleManager.add(Class.forName(bean.getBeanClassName()));
				} catch (Exception e) {
					LOGGER.error("Failed to load Module " + bean.getBeanClassName(), e);
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
