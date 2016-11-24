package br.com.brjdevs.miyuki.utils.log;

import br.com.brjdevs.miyuki.modules.cmds.PushCmd;
import ch.qos.logback.core.AppenderBase;

import static br.com.brjdevs.miyuki.utils.StringUtils.limit;

public class DiscordLogBack extends AppenderBase<Object> {
	public static String latestLog = "";

	@Override
	protected void append(Object eventObject) {

		PushCmd.pushSimple("log", channel -> "**[LOG]** " + limit(eventObject.toString(), 1990));

		latestLog = latestLog.concat(eventObject.toString());
		if (latestLog.length() >= 30000) {
			int cutAt = latestLog.indexOf('\n', 30000 - latestLog.length());
			if (cutAt < 0) cutAt = 30001 - latestLog.length();
			latestLog = latestLog.substring(cutAt);
		}
	}
} 