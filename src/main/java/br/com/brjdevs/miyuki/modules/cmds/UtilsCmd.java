package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Command;
import br.com.brjdevs.miyuki.modules.cmds.util.FeedingUtil;
import br.com.brjdevs.miyuki.utils.HTML2Discord;

@Module(name = "cmds.utils")
public class UtilsCmd {
	@Command("utils")
	private static ICommand utils() {
		return Commands.buildTree()
			.addCommand("convert", Commands.buildTree()
				.addCommand("html2md", Commands.buildSimple("utils.convert.html2md.usage")
					.setAction(event -> event.awaitTyping(false).sendMessage(HTML2Discord.toDiscordFormat(event.getArgs())).queue())
					.build()
				)
				.addCommand("md2text", Commands.buildSimple("utils.convert.md2text.usage")
					.setAction(event -> event.awaitTyping(false).sendMessage(HTML2Discord.toPlainText(event.getArgs())).queue())
					.build()
				)
				.addCommand("html2text", Commands.buildSimple("utils.convert.html2text.usage")
					.setAction(event -> event.awaitTyping(false).sendMessage(HTML2Discord.toPlainText(HTML2Discord.toDiscordFormat(event.getArgs()))).queue())
					.build()
				)
				.build()
			)
			.addCommand("shorten", Commands.buildSimple("utils.shorten.usage")
				.setAction(event -> {
					String r;
					if (event.getArgs(0).length == 1) {
						r = FeedingUtil.shorten(event.getArgs());
					} else if (event.getArgs(0).length == 2) {
						r = FeedingUtil.shorten(event.getArg(2, 0), event.getArg(2, 1));
					} else {
						event.awaitTyping(false).getAnswers().invalidargs().queue();
						return;
					}
					event.awaitTyping(false).getAnswers().bool(!r.contains("Error:"), ": " + r).queue();
				})
				.build()
			)
			.addCommand("hashcode", Commands.buildSimple("utils.hashcode.usage")
				.setAction(event -> event.awaitTyping(false).sendMessage("**HashCode**: " + event.getArgs().hashCode()).queue())
				.build()
			)
			.build();
	}
}
