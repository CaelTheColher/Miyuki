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
