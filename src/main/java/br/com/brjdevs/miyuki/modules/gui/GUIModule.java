package br.com.brjdevs.miyuki.modules.gui;

import br.com.brjdevs.miyuki.Loader;
import br.com.brjdevs.miyuki.commands.Holder;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.*;
import br.com.brjdevs.miyuki.modules.gui.impl.BotGui;
import br.com.brjdevs.miyuki.oldmodules.cmds.PushCmd;
import br.com.brjdevs.miyuki.utils.QueueLogAppender;
import br.com.brjdevs.miyuki.utils.ThreadBuilder;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.Arrays;

@Module(name = "gui", type = Type.STATIC)
public class GUIModule {
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
}
