package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.core.BotInfo;
import br.com.brjdevs.miyuki.core.LoadController;
import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.Command;
import br.com.brjdevs.miyuki.core.commands.Commands;
import br.com.brjdevs.miyuki.core.commands.ICommand;
import br.com.brjdevs.miyuki.core.entities.ModuleContainer;
import br.com.brjdevs.miyuki.lib.Pastee;
import br.com.brjdevs.miyuki.lib.TaskManager;
import br.com.brjdevs.miyuki.lib.log.DiscordLogBack;
import br.com.brjdevs.miyuki.modules.cmds.manager.CommandManager.TooFast;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.cmds.util.SessionManager;
import br.com.brjdevs.miyuki.modules.cmds.utils.scripting.JS;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.modules.init.BotGreeter;
import br.com.brjdevs.miyuki.modules.init.InitModule;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Module(id = "cmds.bot", name = "BotCommand", order = 21)
public class BotCmd {

	@Command("bot")
	private static ICommand createCommand() {
		return Commands.buildTree(PermissionsModule.RUN_CMDS)
			.addCommand("info",
				Commands.buildSimple("bot.info.usage").setAction((event) -> BotGreeter.greet(event.getChannel(), Optional.of(event.getAuthor()))).build()
			)
			.addDefault("info")
			.addCommand("version", Commands.buildSimple("bot.version.usage").setAction(e -> e.getAnswers().send("**Bot Version:** " + BotInfo.VERSION + "\n**JDA Version** " + JDAInfo.VERSION).queue()).build())
			.addCommand("session",
				Commands.buildSimple("bot.session.usage").setAction((event) -> event.awaitTyping(false).sendMessage(new MessageBuilder().setEmbed(SessionManager.createEmbed(event)).build()).queue()).build()
			)
			.addCommand("inviteme",
				Commands.buildSimple("bot.inviteme.usage")
					.setAction(event -> event.getAnswers().send("**" + I18nModule.getLocalized("bot.inviteme.link", event) + ":**\nhttps://discordapp.com/oauth2/authorize?client_id=" + event.getJDA().getSelfUser().getId() + "&scope=bot").queue())
					.build()
			)
			.addCommand("administration", Commands.buildTree(PermissionsModule.BOT_OWNER)
				.addCommand("stop",
					Commands.buildSimple("bot.stop.usage")
						.setAction(event -> {
							event.getAnswers().announce(I18nModule.getLocalized("bot.stop", event)).queue();
							InitModule.stopBot();
						})
						.build()
				)
				.addCommand("restart",
					Commands.buildSimple("bot.stop.usage")
						.setAction(event -> {
							event.getAnswers().announce(I18nModule.getLocalized("bot.stop", event)).queue();
							InitModule.restartBot();
						})
						.build()
				)
				.addCommand("toofast",
					Commands.buildSimple("bot.toofast.usage")
						.setAction((event) -> event.getAnswers().bool(TooFast.enabled = !TooFast.enabled).queue()).build()
				)
				.addCommand("updatecheck",
					Commands.buildSimple("bot.admin.updatecheck.usage")
						.setAction(event -> {
							boolean exists = new File("/var/updates/Miyuki-r.jar").exists();
							event.awaitTyping(true).getAnswers().bool(exists, exists ? " Oh, hey. There's an Update waiting!" : " Meh, no new Jars for me.").queue();
						}).build()
				)
				.addCommand("modules",
					Commands.buildSimple("bot.admin.modules.usage")
						.setAction(event -> {
							event
								.sendMessage(LoadController.modules().stream()
									.map(ModuleContainer::getName)
									.reduce("**Modules Loaded:**", (s1, s2) -> s1 + "\n - " + s2)
								).queue();
						}).build()
				)
				.addCommand("pastelog",
					Commands.buildSimple("bot.admin.pastelog.usage")
						.setAction(event -> {
							Future<String> stringFuture = TaskManager.getThreadPool().submit(() -> Pastee.post(DiscordLogBack.latestLog));
							try {
								event.awaitTyping().getAnswers().bool(true, " Latest Log: " + stringFuture.get()).queue();
							} catch (InterruptedException | ExecutionException ignored) {
								event.awaitTyping().getAnswers().bool(false, " Could not upload the Log \uD83D\uDE26").queue();
								throw new RuntimeException(ignored);
							}
						}).build()
				)
				.build()
			)
			.addCommand("admin", "administration")
			.addCommand("eval",
				Commands.buildSimple("bot.eval.usage", PermissionsModule.BOT_ADMIN)
					.setAction(JS::eval)
					.build()
			)
			.addCommand("ping", Commands.buildSimple()
				.sendStartTyping(false)
				.setAction((event) -> {
					long milis = System.currentTimeMillis();
					event.sendTyping().queue(
						success -> {
							long time = System.currentTimeMillis() - milis;
							event.sendMessage("Found out that my ping is " + time + "ms, " + ratePing(time)).queue();
						}
					);
				}).build())
			.build();
	}

	private static String ratePing(long ping) {
		if (ping < 0) return "which doesn't even make any sense at all.";
		if (ping < 10) return "which is faster than Sonic.";
		if (ping < 100) return "which is great!";
		if (ping < 200) return "which is nice!";
		if (ping < 300) return "which is good!";
		if (ping < 400) return "which is average...";
		if (ping < 500) return "which is not that bad.";
		if (ping < 600) return "which is kinda slow..";
		if (ping < 700) return "which is not that fast..";
		if (ping < 800) return "which is slow.";
		if (ping < 800) return "which is awful.";
		if (ping < 900) return "which is bad.";
		return "which is slow af";
	}
}
