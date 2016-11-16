/*
 * This class was created by <AdrianTodt>. It's distributed as
 * part of the DavidBot. Get the Source Code in github:
 * https://github.com/adriantodt/David
 *
 * DavidBot is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/adriantodt/David/blob/master/LICENSE
 *
 * File Created @ [08/11/16 22:30]
 */

package br.com.brjdevs.miyuki.David.modules.cmds;

import br.com.brjdevs.miyuki.David.Info;
import br.com.brjdevs.miyuki.David.commands.base.Commands;
import br.com.brjdevs.miyuki.David.commands.base.ICommand;
import br.com.brjdevs.miyuki.David.loader.Module;
import br.com.brjdevs.miyuki.David.loader.Module.Command;
import br.com.brjdevs.miyuki.David.loader.Module.Type;
import br.com.brjdevs.miyuki.David.modules.cmds.manager.CommandManager.TooFast;
import br.com.brjdevs.miyuki.David.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.David.modules.db.I18nModule;
import br.com.brjdevs.miyuki.David.modules.init.BotGreeter;
import br.com.brjdevs.miyuki.David.modules.init.InitModule;
import br.com.brjdevs.miyuki.David.oldmodules.cmds.utils.scripting.JS;
import br.com.brjdevs.miyuki.David.oldmodules.init.Statistics;
import net.dv8tion.jda.core.JDAInfo;

import java.util.Optional;

@Module(name = "cmds.bot", type = Type.STATIC)
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
