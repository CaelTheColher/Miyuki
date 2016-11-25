package br.com.brjdevs.miyuki.modules.cmds.manager;

import br.com.brjdevs.miyuki.core.Module;
import br.com.brjdevs.miyuki.core.Module.LoggerInstance;
import br.com.brjdevs.miyuki.core.commands.CommandEvent;
import br.com.brjdevs.miyuki.core.commands.ICommand;
import br.com.brjdevs.miyuki.core.commands.UserCommand;
import br.com.brjdevs.miyuki.lib.TaskManager;
import br.com.brjdevs.miyuki.modules.cmds.util.SessionManager;
import br.com.brjdevs.miyuki.modules.db.GuildModule;
import br.com.brjdevs.miyuki.modules.db.UserCommandsModule;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.com.brjdevs.miyuki.lib.AsyncUtils.async;
import static br.com.brjdevs.miyuki.lib.AsyncUtils.asyncSleepThen;
import static br.com.brjdevs.miyuki.lib.CollectionUtils.concatMaps;
import static br.com.brjdevs.miyuki.lib.StringUtils.splitArgs;

@Module(id = "cmdmanager", isListener = true, order = 1)
public class CommandManager {
	private static final Map<String, ICommand> COMMANDS = new HashMap<>();
	@LoggerInstance
	private static Logger logger = null;

	@SubscribeEvent
	private static void onMessageReceived(GuildMessageReceivedEvent msgEvent) {
		logger.info(msgEvent.toString());
		if (!PermissionUtil.checkPermission(msgEvent.getChannel(), msgEvent.getGuild().getSelfMember(), Permission.MESSAGE_WRITE))
			return;

		if (msgEvent.getAuthor().equals(msgEvent.getJDA().getSelfUser())) {
			asyncSleepThen(15 * 1000, () -> {
				if (GuildModule.fromDiscord(msgEvent.getGuild()).getFlag("cleanup"))
					msgEvent.getMessage().deleteMessage();
			}).run();
			return;
		} else if (msgEvent.getAuthor().isBot()) {
			return;
		}

		async(() -> onCommand(msgEvent)).run();
	}

	private static void onCommand(GuildMessageReceivedEvent msgEvent) {
		Thread.currentThread().setName(msgEvent.getAuthor().getName() + ">[unknown]");
		GuildModule.Data local = GuildModule.fromDiscord(msgEvent.getGuild()), global = GuildModule.GLOBAL, target = local;
		if (!PermissionsModule.havePermsRequired(global, msgEvent.getAuthor(), PermissionsModule.RUN_CMDS) || !PermissionsModule.havePermsRequired(local, msgEvent.getAuthor(), PermissionsModule.RUN_CMDS))
			return;

		String cmd = msgEvent.getMessage().getRawContent();

		List<String> prefixes = new ArrayList<>(local.getCmdPrefixes());
		prefixes.add("<@!" + msgEvent.getJDA().getSelfUser().getId() + "> ");
		prefixes.add("<@" + msgEvent.getJDA().getSelfUser().getId() + "> ");
		boolean isCmd = false;
		for (String prefix : prefixes) {
			if (cmd.startsWith(prefix)) {
				cmd = cmd.substring(prefix.length());
				isCmd = true;
				break;
			}
		}

		if (isCmd) {
			String baseCmd = splitArgs(cmd, 2)[0];
			//GuildWorksTM
			if (baseCmd.indexOf(':') != -1) {
				String guildname = baseCmd.substring(0, baseCmd.indexOf(':'));
				baseCmd = baseCmd.substring(baseCmd.indexOf(':') + 1);

				GuildModule.Data guild = GuildModule.fromName(guildname);
				if (guild != null && (PermissionsModule.havePermsRequired(guild, msgEvent.getAuthor(), PermissionsModule.GUILD_PASS) || PermissionsModule.havePermsRequired(global, msgEvent.getAuthor(), PermissionsModule.GUILD_PASS)))
					target = guild;
			}

			logger.info("baseCmd = " + baseCmd + "; target = " + target.getName());
			ICommand command = getCommands(target).get(baseCmd.toLowerCase());
			logger.info("command = " + command);
			if (command != null) {
				CommandEvent event = new CommandEvent(msgEvent, target, command, splitArgs(cmd, 2)[1]);
				if (!PermissionsModule.canRunCommand(target, event)) event.getAnswers().noperm().queue();
				else if (TooFast.enabled && !TooFast.canExecuteCmd(msgEvent)) event.getAnswers().toofast().queue();
				else {
					if (event.getCommand().sendStartTyping()) event.sendAwaitableTyping();
					SessionManager.cmds++;
					Thread.currentThread().setName(event.getAuthor().getName() + ">" + baseCmd);
					try {
						execute(event);
					} catch (Exception e) {
						event.getAnswers().exception(e).queue();
					}
				}
			}
		}
	}

	public static void addCommand(String name, ICommand command) {
		COMMANDS.put(name.toLowerCase(), command);
	}

	public static Map<String, ICommand> getCommands(GuildModule.Data guild) {
		return concatMaps(getBaseCommands(), new HashMap<>(getUserCommands(guild)));
	}

	public static Map<String, ICommand> getBaseCommands() {
		return COMMANDS;
	}

	public static Map<String, UserCommand> getUserCommands(GuildModule.Data guild) {
		return concatMaps(getGlobalUserCommands(), getLocalUserCommands(guild));
	}

	public static Map<String, UserCommand> getGlobalUserCommands() {
		return getLocalUserCommands(GuildModule.GLOBAL);
	}

	public static Map<String, UserCommand> getLocalUserCommands(GuildModule.Data guild) {
		return UserCommandsModule.allFrom(guild);
	}

	public static void execute(CommandEvent event) {
		if (PermissionsModule.canRunCommand(GuildModule.GLOBAL, event) || PermissionsModule.canRunCommand(event.getGuild(), event))
			event.getCommand().run(event);
		else event.getAnswers().noperm().queue();
	}

	public static class TooFast {
		public static final Map<User, Integer> userTimeout = new HashMap<>();
		public static boolean enabled = true;

		static {
			TaskManager.startAsyncTask("User Timeout", () -> {
				synchronized (TooFast.userTimeout) {
					TooFast.userTimeout.replaceAll((user, integer) -> Math.max(0, integer - 1));
				}
			}, 5);
		}

		public static boolean canExecuteCmd(GuildMessageReceivedEvent event) {
			int count;
			synchronized (userTimeout) {
				count = userTimeout.getOrDefault(event.getAuthor(), 0);
				userTimeout.put(event.getAuthor(), count + 1);
			}
			return count + 1 < 5;
		}
	}
}
