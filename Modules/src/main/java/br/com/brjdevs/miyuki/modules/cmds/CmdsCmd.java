package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.framework.Module;
import br.com.brjdevs.miyuki.framework.Module.Command;
import br.com.brjdevs.miyuki.lib.Formatter;
import br.com.brjdevs.miyuki.lib.Holder;
import br.com.brjdevs.miyuki.modules.cmds.manager.CommandManager;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.cmds.manager.entities.Commands;
import br.com.brjdevs.miyuki.modules.cmds.manager.entities.ICommand;
import br.com.brjdevs.miyuki.modules.cmds.manager.entities.UserCommand;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import br.com.brjdevs.miyuki.modules.db.UserCommandsModule;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.brjdevs.miyuki.modules.db.I18nModule.getLocalized;

@Module(id = "cmds.cmds", name = "CmdsCommand", order = 22)
public class CmdsCmd {
	@Command("cmds")
	private static ICommand createCommand() {
		return Commands.buildTree()
			.addCommand("list",
				Commands.buildSimple("cmds.list.usage").setAction(event -> {
					List<String> cmds, userCmds, tmp = new ArrayList<>();
					cmds = CommandManager.getCommands(event.getGuild()).entrySet().stream().filter(entry -> PermissionsModule.canRunCommand(event.getGuild(), event.createChild(entry.getValue(), event.getArgs()))).filter(entry -> {
						if (entry.getValue() instanceof UserCommand) {
							tmp.add(entry.getKey());
							return false;
						}
						return true;
					}).map(Map.Entry::getKey).sorted(String::compareTo).collect(Collectors.toList());

					userCmds = tmp.stream().sorted(String::compareTo).collect(Collectors.toList());

					Holder<StringBuilder> b = new Holder<>();
					Holder<Boolean> first = new Holder<>();

					b.var = new StringBuilder().append("**").append(getLocalized("cmds.commandsAvailable", event)).append(":**\n *");
					first.var = true;
					cmds.forEach(s -> {
						if (first.var) {
							first.var = false;
							b.var.append(s);
						} else {
							String a = " " + s;
							if (b.var.length() + a.length() >= 1999) {
								b.var.append("*");
								event.getAnswers().send(b.var.toString()).queue();
								b.var = new StringBuilder("*");
							}
							b.var.append(a);
						}

					});
					if (first.var) b.var.append("(").append(getLocalized("cmds.noneAvailable", event)).append(")");
					b.var.append("*");
					event.getAnswers().send(b.var.toString()).queue();

					b.var = new StringBuilder().append("**").append(getLocalized("cmds.userCommandsAvailable", event)).append(":**\n *");
					first.var = true;

					userCmds.forEach(s -> {
						if (first.var) {
							first.var = false;
							b.var.append(s);
						} else {
							String a = " " + s;
							if (b.var.length() + a.length() >= 1999) {
								b.var.append("*");
								event.getAnswers().send(b.var.toString()).queue();
								b.var = new StringBuilder("*");
							}
							b.var.append(a);
						}

					});
					if (first.var) b.var.append("(").append(getLocalized("cmds.noneAvailable", event)).append(")");
					b.var.append("*");
					event.getAnswers().send(b.var.toString()).queue();
				}).build())
			.addDefault("list")
			.addCommand("detailed", Commands.buildSimple("cmds.detailed.usage").setAction(event -> {
				if (!event.tryOpenPrivateChannel()) return;

				MessageChannel channel = event.getAuthor().getPrivateChannel();
				List<String> cmds = CommandManager.getBaseCommands().entrySet().stream().filter(entry -> PermissionsModule.canRunCommand(event.getGuild(), event.createChild(entry.getValue(), event.getArgs()))).map(
					(entry) -> entry.getKey() + " - " + entry.getValue().toString(I18nModule.getLocale(event))).sorted(String::compareTo).collect(Collectors.toList());

				Holder<StringBuilder> b = new Holder<>(new StringBuilder().append("**").append(getLocalized("cmds.commandsAvailable", event)).append(":**\n"));
				Holder<Boolean> first = new Holder<>(true);

				cmds.forEach(s -> {
					String v = Formatter.encase(s);
					if (first.var) {
						first.var = false;
						b.var.append(v);
					} else {
						if (b.var.length() + v.length() >= 1995) {
							channel.sendMessage(b.var.toString()).queue();
							b.var = new StringBuilder();
						}
						b.var.append(v);
					}

				});
				if (first.var) b.var.append("(").append(getLocalized("cmds.noneAvailable", event)).append(")");
				channel.sendMessage(b.var.toString()).queue();
				event.getAnswers().send(event.getAuthor().getAsMention() + " :mailbox_with_mail:").queue();
			}).build())
			.addCommand("add", Commands.buildSimple("cmds.add.usage", PermissionsModule.MANAGE_USER_CMDS)
				.setAction(event -> {
					String[] args = event.getArgs(2); //COMMAND_NAME RESPONSE
					if (args[0].isEmpty() | args[1].isEmpty()) event.getAnswers().invalidargs().queue();
					else {
						if (Stream.of("loc://", "js://", "aud://").anyMatch(args[1]::startsWith) && !PermissionsModule.havePermsRequired(event.getGuild(), event.getAuthor(), PermissionsModule.MANAGE_SPECIAL_USER_CMDS)) {
							event.awaitTyping(false).getAnswers().noperm(PermissionsModule.MANAGE_SPECIAL_USER_CMDS).queue();
							return;
						}

						UserCommand cmd = CommandManager.getLocalUserCommands(event.getGuild()).get(args[0].toLowerCase());
						if (cmd == null) {
							UserCommand ncmd = new UserCommand();
							ncmd.responses.add(args[1]);
							UserCommandsModule.register(ncmd, args[0].toLowerCase(), event.getGuild());
							event.getAnswers().bool(true).queue();
						} else {
							cmd.responses.add(args[1]);
							UserCommandsModule.update(cmd);
							event.getAnswers().bool(true).queue();
						}
					}
				}).build())
			.addCommand("rm", Commands.buildSimple("cmds.rm.usage", PermissionsModule.MANAGE_USER_CMDS)
				.setAction(event -> {
					if (event.getArgs().trim().isEmpty()) event.getAnswers().invalidargs().queue();
					else {
						UserCommand command = CommandManager.getLocalUserCommands(event.getGuild()).get(event.getArgs().toLowerCase());
						if (command == null) event.getAnswers().invalidargs().queue();
						else {
							UserCommandsModule.remove(command);
							event.getAnswers().bool(true).queue();
						}
					}
				}).build())
			.addCommand("debug", Commands.buildSimple("cmds.debug.usage", PermissionsModule.MANAGE_USER_CMDS).setAction(event -> {
				if (event.getArgs().trim().isEmpty()) event.getAnswers().invalidargs().queue();
				else {
					ICommand cmd = CommandManager.getCommands(event.getGuild()).get(event.getArgs());
					if (cmd == null) event.getAnswers().invalidargs().queue();
					else
						event.getAnswers().send("***`" + event.getArgs() + "`:*** " + cmd.toString(I18nModule.getLocale(event))).queue();
				}
			}).build())
			.build();
	}
}
