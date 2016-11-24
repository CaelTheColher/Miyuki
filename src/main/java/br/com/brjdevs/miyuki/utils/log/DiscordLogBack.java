package br.com.brjdevs.miyuki.utils.log;

import br.com.brjdevs.miyuki.modules.cmds.PushCmd;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import static br.com.brjdevs.miyuki.utils.StringUtils.limit;

public class DiscordLogBack extends AppenderBase<ILoggingEvent> {
	public static String latestLog = "";
	private PatternLayout patternLayout;

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
		PushCmd.pushSimple("log", channel -> "**[LOG]** " + limit(msg, 1990));

		latestLog = latestLog.concat(msg);
		if (latestLog.length() >= 30000) {
			int cutAt = latestLog.indexOf('\n', 30000 - latestLog.length());
			if (cutAt < 0) cutAt = 30001 - latestLog.length();
			latestLog = latestLog.substring(cutAt);
		}
	}
}