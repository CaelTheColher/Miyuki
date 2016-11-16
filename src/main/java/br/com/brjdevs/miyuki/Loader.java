package br.com.brjdevs.miyuki;

import br.com.brjdevs.java.lib.IOHelper;
import br.com.brjdevs.miyuki.loader.ModuleManager;
import br.com.brjdevs.miyuki.modules.db.DBModule;
import br.com.brjdevs.miyuki.utils.Log4jUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.apache.logging.log4j.Logger;

public class Loader {
	public static final Logger LOGGER = Log4jUtils.logger();
	public static String[] args;

	public static void main(String[] args) throws Exception {
		Loader.args = args;
		JsonElement src = new JsonParser().parse(resource(Loader.class, "/assets/loader/main.json"));

		if (!src.isJsonArray()) {
			LOGGER.error("\"/assets/loader/main.json\" is in a incorrect form. Expected \"" + JsonArray.class + "\", got \"" + src.getClass() + "\"");
			return;
		}

		src.getAsJsonArray().forEach(element -> {
			try {
				ModuleManager.add(Class.forName(element.getAsString()));
			} catch (Exception e) {
				LOGGER.error("Failed to load Module " + element, e);
			}
		});

		ModuleManager.firePreReadyEvents();

		new JDABuilder(AccountType.BOT)
			.setToken(DBModule.getConfig().get("token").getAsString())
			.setEventManager(new AnnotatedEventManager())
			.addListener(ModuleManager.jdaListeners())
			.buildBlocking();

		ModuleManager.firePostReadyEvents();
	}

	public static String resource(Class c, String file) {
		return IOHelper.toString(c.getResourceAsStream(file));
	}
}
