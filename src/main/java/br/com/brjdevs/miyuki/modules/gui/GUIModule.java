package br.com.brjdevs.miyuki.modules.gui;

import br.com.brjdevs.miyuki.Loader;
import br.com.brjdevs.miyuki.commands.Holder;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.*;
import br.com.brjdevs.miyuki.modules.gui.impl.BotGui;
import br.com.brjdevs.miyuki.modules.cmds.PushCmd;
import br.com.brjdevs.miyuki.utils.QueueLogAppender;
import br.com.brjdevs.miyuki.utils.ThreadBuilder;
import net.dv8tion.jda.core.JDA;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(name = "gui")
public class GUIModule {
	@JDAInstance
	public static JDA jda = null;
	public static boolean loaded = false;
	public static List<Runnable> hooks = new ArrayList<>(), lazyHooks = new ArrayList<>();
	@LoggerInstance
	private static Logger logger = null;
	private static BotGui UI;

	@Predicate
	public static boolean enable() {
		return !GraphicsEnvironment.isHeadless() && Arrays.stream(Loader.args).filter("nogui"::equals).findAny().isPresent();
	}

	@OnEnabled
	public static void enabled() {
		logger.info("Loading GUI...");
		UI = BotGui.createBotGui();

		new ThreadBuilder().setDaemon(true).setName("Log4j2Discord").build(() -> new Thread(() -> {
			System.out.println("Log4j2Discord Enabled!");
			Holder<String> s = new Holder<>();
			while ((s.var = QueueLogAppender.getNextLogEvent("DiscordLogListeners")) != null) {
				PushCmd.pushSimple("get", channel -> "[LOG] " + s.var);
			}
			System.out.println("Log4j2Discord Disabled...");
		})).start();
	}

	@OnDisabled
	public static void disabled() {
		if (GraphicsEnvironment.isHeadless()) {
			logger.info("GUI Disabled. (Headless Environiment)");
		} else logger.info("GUI Disabled. (parameter \"nogui\")");
	}

	@Ready
	private static void ready() {
		loaded = true;
		hooks.forEach(Runnable::run);
		hooks = null;
	}

	@PostReady
	private static void postReady() {
		lazyHooks.forEach(Runnable::run);
		lazyHooks = null;
	}
}
