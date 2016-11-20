package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Command;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.db.GuildModule;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.utils.data.Commitable;
import net.dv8tion.jda.core.MessageBuilder;

import java.util.Arrays;
import java.util.List;

@Module(name = "cmds.guild")
public class GuildCmd {
	@Command("guild")
	private static ICommand createCommand() {
		return Commands.buildTree()
			.addCommand("info",
				Commands.buildSimple("guild.info.usage")
					.setAction(event -> event.awaitTyping(false).sendMessage(new MessageBuilder().setEmbed(GuildModule.createEmbed(event.getGuild(), event.getJDA(), I18nModule.getLocale(event))).build()).queue())
					.build()
			)
			.addDefault("info")
			.addCommand("lang",
				Commands.buildSimple("guild.lang.usage", PermissionsModule.SET_GUILD)
					.setAction(event -> {
						event.getGuild().setLang(event.getArgs().isEmpty() ? "en_US" : event.getArgs());
						event.awaitTyping(false).getAnswers().announce(String.format(I18nModule.getLocalized("guild.lang.set", event), event.getGuild().getLang())).queue();
					}).build()
			)
			.addCommand("cleanup",
				Commands.buildSimple("guild.cleanup.usage", PermissionsModule.SET_GUILD)
					.setAction(event -> event.awaitTyping(false).getAnswers().bool(event.getGuild().toggleFlag("cleanup")).queue())
					.build()
			)
			.addCommand("prefixes",
				Commands.buildSimple("guild.prefixes.usage", PermissionsModule.SET_GUILD)
					.setAction(event -> {
						if (event.getArgs().trim().isEmpty()) {
							event.awaitTyping(false).getAnswers().invalidargs().queue();
							return;
						}
						Commitable<List<String>> cmdPrefixesHandler = event.getGuild().modifyCmdPrefixes();
						List<String> cmdPrefixes = cmdPrefixesHandler.get();
						String[] all = event.getArgs(-1);
						for (String each : all) {
							if (each.toLowerCase().equals("+default")) {
								Arrays.asList(GuildModule.DEFAULT_PREFIXES).forEach(s -> {
									if (!cmdPrefixes.contains(s)) cmdPrefixes.add(s);
								});
							} else if (each.charAt(0) == '+') {
								String v = each.substring(1);
								if (!cmdPrefixes.contains(v)) cmdPrefixes.add(v);
							} else if (each.toLowerCase().equals("clear")) {
								cmdPrefixes.clear();
							} else if (each.toLowerCase().equals("list") || each.toLowerCase().equals("get")) {
								event.awaitTyping(false).getAnswers().send(Arrays.toString(cmdPrefixes.toArray())).queue();
							}
						}
						cmdPrefixesHandler.pushChanges();
						event.awaitTyping(false).getAnswers().bool(true).queue();
					})
					.build()
			)
			.addCommand("perms", Commands.buildTree()
				.addCommand("get", Commands.buildSimple("guild.perms.get.usage")
					.setAction(event -> {
						String arg = event.getArg(1, 0); //!getlevel USER
						if (arg.isEmpty()) arg = event.getAuthor().getId();
						event.getAnswers().send("**" + I18nModule.getLocalized("guild.perms.get.userPerms", event) + ":**\n *" + String.join(", ", PermissionsModule.toCollection(PermissionsModule.getPermFor(event.getGuild(), arg)).stream().toArray(String[]::new)) + "*").queue();
					}).build())
				.addCommand("set", Commands.buildSimple("guild.perms.set.usage", PermissionsModule.SET_PERMS)
					.setAction(event -> {
						String[] args = event.getArgs(2); //!setlevel USER LEVEL
						if (args[0].isEmpty() || args[1].isEmpty()) event.getAnswers().invalidargs().queue();
						else {
							String[] all = args[1].split("\\s+", -1);
							int toBeSet = 0, toBeUnset = 0;
							for (String each : all) {
								if (each.charAt(0) == '+') {
									String p = each.substring(1).toUpperCase();
									if (PermissionsModule.perms.containsKey(p))
										toBeSet |= PermissionsModule.perms.get(p);
								} else if (each.charAt(0) == '-') {
									String p = each.substring(1).toUpperCase();
									if (PermissionsModule.perms.containsKey(p))
										toBeUnset |= PermissionsModule.perms.get(p);
								}
							}
							event.getAnswers().bool(PermissionsModule.setPerms(event.getGuild(), event, args[0], toBeSet, toBeUnset)).queue();
						}
					})
					.build()
				)
				.addCommand("list", Commands.buildSimple("guild.perms.list.usage")
					.setAction(event -> event.getAnswers().send("**" + I18nModule.getLocalized("guild.perms.get.userPerms", event) + ":**\n *" + String.join(", ", PermissionsModule.toCollection(PermissionsModule.BOT_OWNER).stream().toArray(String[]::new)) + "*").queue())
					.build()
				)
				.build()
			)
			.build();
	}
}
