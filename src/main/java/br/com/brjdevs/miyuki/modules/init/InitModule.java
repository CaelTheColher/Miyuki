package br.com.brjdevs.miyuki.modules.init;

import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.*;
import br.com.brjdevs.miyuki.modules.cmds.util.SessionManager;
import br.com.brjdevs.miyuki.modules.db.DBModule;
import br.com.brjdevs.miyuki.utils.CollectionUtils;
import br.com.brjdevs.miyuki.utils.DiscordUtils;
import br.com.brjdevs.miyuki.utils.Java;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

@Module(id = "init", isListener = true)
public class InitModule {
	@JDAInstance
	private static JDA jda = null;

	@LoggerInstance
	private static Logger logger = null;

	@SelfUserInstance
	private static SelfUser user = null;

	@OnEnabled
	public static void init() {
		logger.info("Pre-Initializating...");
		try {
			File file = new File("./tmp/");
			if (file.exists())
				delete(file);
			//noinspection ResultOfMethodCallIgnored
			file.mkdir();
			System.setProperty("java.io.tmpdir", file.getCanonicalPath());
		} catch (Exception e) {
			logger.error("Error while trying to define TMPDir: ", e);
		}
		logger.info("TMP Directory: " + System.getProperty("java.io.tmpdir"));

		DiscordUtils.hackJDALog();
	}

	@Ready
	public static void ready() {
		SessionManager.startDate = new Date();
		logger.info("Bot: " + user.getName() + " (#" + jda.getSelfUser().getId() + ")");
		jda.getPresence().setGame(Game.of("mention me for help"));
	}

	@PostReady
	public static void postReady() {
		Set<User> owners = DBModule.getOwners();
		if (owners.size() == 0) {
			logger.warn("Owner(s) not regognized. This WILL cause issues (specially PermSystem)");
		} else {
			logger.info("Owner(s) recognized: " + CollectionUtils.toString(owners, (user -> user.getName() + "#" + user.getDiscriminator() + " (ID: " + user.getId() + ")"), ", "));
		}

		//Pushes.pushSimple("start", channel -> I18nModule.getLocalized("bot.startup", channel));
	}

	private static void delete(File f) throws IOException {
		if (f.isDirectory()) //noinspection ConstantConditions
			for (File c : f.listFiles()) delete(c);
		if (!f.delete()) throw new FileNotFoundException("Failed to delete file: " + f);
	}

	public static void stopBot() {
		jda.getPresence().setGame(Game.of("Stopping..."));
		jda.getPresence().setIdle(true);
		logger.info("Bot exiting...");
		//Pushes.pushSimple("stop", channel -> boldAndItalic(I18nModule.getLocalized("bot.stop", channel)));
		try {
			Thread.sleep(2 * 1000);
		} catch (Exception ignored) {
		}
		jda.shutdownNow(true);
		Java.stopApp();
	}

	public static void restartBot() {
		jda.getPresence().setGame(Game.of("Restarting..."));
		jda.getPresence().setIdle(true);
		logger.info("Bot restarting...");
		//Pushes.pushSimple("stop", channel -> boldAndItalic(I18nModule.getLocalized("bot.stop", channel)));
		try {
			Thread.sleep(2 * 1000);
		} catch (Exception ignored) {
		}
		jda.shutdownNow(true);
		Java.restartApp();
	}
}
