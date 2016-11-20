package br.com.brjdevs.miyuki.modules.cmds.util;


import br.com.brjdevs.miyuki.commands.CommandEvent;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.db.GuildModule;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.modules.db.UserModule;
import br.com.brjdevs.miyuki.utils.DataFormatter;
import br.com.brjdevs.miyuki.utils.TaskManager;
import com.sun.management.OperatingSystemMXBean;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

//TODO CLEANUP THIS WHOLE SHIT. TOO MUCH TO FIX.
@SuppressWarnings("unchecked")
public class SessionManager {

	public static Date startDate = null;
	//public static int loads = 0, saves = 0, crashes = 0, noperm = 0, invalidargs = 0, msgs = 0, cmds = 0, wgets = 0, toofasts = 0;
	public static int restActions = 0, toofasts = 0, cmds = 0, crashes = 0, noperm = 0, invalidargs = 0, wgets = 0;

	public static double cpuUsage = 0;

	static {
		try {
			Field field = RestAction.class.getField("DEFAULT_SUCCESS");
			field.setAccessible(true);

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			field.set(null, RestAction.DEFAULT_SUCCESS.andThen(o -> SessionManager.restActions++));
		} catch (Exception e) {
			LogManager.getLogger("Statistics-BruteReflections").error("The hacky heavy reflection static code block crashed. #BlameSpong and #BlameMinn", e);
		}

		final OperatingSystemMXBean os = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
		TaskManager.startAsyncTask("CPU Usage", () -> cpuUsage = (Math.floor(os.getProcessCpuLoad() * 10000) / 100), 2);
	}

	public static String calculate(Date startDate, Date endDate, String language) {

		//milliseconds
		long different = endDate.getTime() - startDate.getTime();

		if (different <= 0) {
			return I18nModule.getLocalized("stats.negativeTime", language);
		}

		different = different / 1000;
		long minutesInMilli = 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different;

		return String.format(
			I18nModule.getLocalized("stats.timeFormat", language),
			elapsedDays,
			elapsedHours, elapsedMinutes, elapsedSeconds);

	}

	public static int clampIfNotOwner(int value, int min, int max, User user) {
		if (PermissionsModule.havePermsRequired(GuildModule.GLOBAL, user, PermissionsModule.GUILD_OWNER)) return value;
		return Math.min(max, Math.max(min, value));
	}

	public static int parseInt(String s, int onCatch) {
		try {
			return Integer.parseInt(s);
		} catch (Exception ignored) {
		}
		return onCatch;
	}

	public static MessageEmbed createEmbed(CommandEvent event) {
		String lang = I18nModule.getLocale(event);
		JDA jda = event.getJDA();
		int mb = 1024 * 1024;
		Runtime instance = Runtime.getRuntime();
		EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.getOriginGuild().getSelfMember().getColor() == null ? Color.decode("#f1c40f") : event.getOriginGuild().getSelfMember().getColor());
		builder.setFooter("Requested by " + event.getAuthor().getName() + " at " + DataFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)), UserModule.getAvatarUrl(event.getAuthor()));
		builder.addField(I18nModule.getLocalized("bot.session.uptime", lang), SessionManager.calculate(SessionManager.startDate, new Date(), lang), false);
		builder.addField(I18nModule.getLocalized("bot.session.users", lang), jda.getUsers().size() + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.restactions", lang), SessionManager.restActions + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.executedcmds", lang), SessionManager.cmds + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.crashes", lang), SessionManager.crashes + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.toofasts", lang), SessionManager.toofasts + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.wgets", lang), SessionManager.wgets + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.activethreads", lang), Thread.activeCount() + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.noperm", lang), SessionManager.noperm + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.guilds", lang), jda.getGuilds().size() + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.channels", lang), jda.getTextChannels().size() + "", true);
		builder.addField(I18nModule.getLocalized("bot.session.ram", lang), ((instance.totalMemory() - instance.freeMemory()) / mb) + " MB/" + (instance.totalMemory() / mb) + " MB/" + (instance.maxMemory() / mb) + " MB", true);
		builder.addField(I18nModule.getLocalized("bot.session.cpu", lang), cpuUsage + "%", true);
		return builder.build();
	}
}
