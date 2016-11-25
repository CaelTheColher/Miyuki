package br.com.brjdevs.miyuki.lib.log;

import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.PostReady;
import br.com.brjdevs.miyuki.lib.Pastee;
import br.com.brjdevs.miyuki.modules.cmds.PushCmd;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import static br.com.brjdevs.miyuki.lib.StringUtils.limit;

@Module(id = "discordLog", order = 40)
public class DiscordLogBack extends AppenderBase<ILoggingEvent> {
	public static String latestLog = "";
	private static StringBuilder preInit = new StringBuilder();
	private static boolean loaded = false;
	private PatternLayout patternLayout;

	@PostReady
	private static void ready() {
		loaded = true;
		String s = preInit.toString();
		PushCmd.pushSimple("log", "**[InitLog]** My InitLog: " + Pastee.post(s));
	}

	@Override
	public void start() {
		patternLayout = new PatternLayout();
		patternLayout.setContext(getContext());
		patternLayout.setPattern("[%d{HH:mm:ss}] [%t/%level] [%logger{0}]: %msg%n");
		patternLayout.start();

		super.start();
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (!event.getLevel().isGreaterOrEqual(Level.DEBUG)) return;

		// Formata mensagem do log
		String msg = patternLayout.doLayout(event);

		latestLog = latestLog.concat(msg);
		if (latestLog.length() >= 1000000) {
			int cutAt = latestLog.indexOf('\n', 1000000 - latestLog.length());
			if (cutAt < 0) cutAt = 1000001 - latestLog.length();
			latestLog = latestLog.substring(cutAt);
		}

		if (loaded) {
			PushCmd.pushSimple("log", channel -> "**[LOG]** " + limit(msg, 1990));
		} else {
			preInit.append(msg);
		}
	}
}