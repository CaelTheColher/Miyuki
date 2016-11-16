package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.Info;
import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Command;
import br.com.brjdevs.miyuki.modules.cmds.manager.CommandManager.TooFast;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.modules.init.BotGreeter;
import br.com.brjdevs.miyuki.modules.init.InitModule;
import br.com.brjdevs.miyuki.oldmodules.cmds.utils.scripting.JS;
import br.com.brjdevs.miyuki.oldmodules.init.Statistics;
import net.dv8tion.jda.core.JDAInfo;

import java.util.Optional;

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
			.addCommand("stop",
				Commands.buildSimple("bot.stop.usage", PermissionsModule.STOP_BOT)
					.setAction(event -> {
						event.getAnswers().announce(I18nModule.getLocalized("bot.stop", event)).queue();
						InitModule.stopBot();
					})
					.build()
			)
			.addCommand("toofast",
				Commands.buildSimple("bot.toofast.usage", PermissionsModule.BOT_OWNER)
					.setAction((event) -> event.getAnswers().bool(TooFast.enabled = !TooFast.enabled).queue()).build()
			)
			.addCommand("session",
				Commands.buildSimple("bot.session.usage").setAction(Statistics::printStats).build()
			)
			.addCommand("inviteme",
				Commands.buildSimple("bot.inviteme.usage")
					.setAction(event -> event.getAnswers().send("**" + I18nModule.getLocalized("bot.inviteme.link", event) + ":**\nhttps://discordapp.com/oauth2/authorize?client_id=" + event.getJDA().getSelfUser().getId() + "&scope=bot").queue())
					.build()
			)
			.addCommand("administration", Commands.buildTree()
				.build()
			)
			.addCommand("eval",
				Commands.buildSimple("bot.eval.usage", PermissionsModule.SCRIPTS | PermissionsModule.RUN_SCRIPT_CMDS | PermissionsModule.SCRIPTS_UNSAFEENV)
					.setAction(JS::eval)
					.build()
			)
			.build();
	}
}
