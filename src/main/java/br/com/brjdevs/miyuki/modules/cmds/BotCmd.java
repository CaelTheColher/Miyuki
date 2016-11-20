package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.Info;
import br.com.brjdevs.miyuki.Loader;
import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Command;
import br.com.brjdevs.miyuki.modules.cmds.manager.CommandManager.TooFast;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.cmds.util.SessionManager;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.modules.init.BotGreeter;
import br.com.brjdevs.miyuki.modules.init.InitModule;
import br.com.brjdevs.miyuki.oldmodules.cmds.utils.scripting.JS;
import br.com.brjdevs.miyuki.utils.Hastebin;
import br.com.brjdevs.miyuki.utils.TaskManager;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Module(name = "cmds.bot")
public class BotCmd {

	@Command("bot")
	private static ICommand createCommand() {
		return Commands.buildTree(PermissionsModule.RUN_CMDS)
			.addCommand("info",
				Commands.buildSimple("bot.info.usage").setAction((event) -> BotGreeter.greet(event.getChannel(), Optional.of(event.getAuthor()))).build()
			)
			.addDefault("info")
			.addCommand("version", Commands.buildSimple("bot.version.usage").setAction(e -> e.getAnswers().send("**Bot Version:** " + Info.VERSION + "\n**JDA Version** " + JDAInfo.VERSION).queue()).build())
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
				.addCommand("pastelog",
					Commands.buildSimple("bot.admin.pastelog.usage")
						.setAction(event -> {
							Future<String> stringFuture = TaskManager.getThreadPool().submit(() -> Hastebin.post(Loader.latestLog));
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
				Commands.buildSimple("bot.eval.usage", PermissionsModule.SCRIPTS | PermissionsModule.RUN_SCRIPT_CMDS | PermissionsModule.SCRIPTS_UNSAFEENV)
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
