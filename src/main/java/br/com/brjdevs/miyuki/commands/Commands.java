/*
 * This class was created by <$user.name>. It's distributed as
 * part of the Miyuki Bot. Get the Source Code in github:
 * https://github.com/BRjDevs/Miyuki
 *
 * Miyuki is Open Source and distributed under the
 * GNU Lesser General Public License v2.1:
 * https://github.com/BRjDevs/Miyuki/blob/master/LICENSE
 *
 * File Created @ [16/11/16 13:58]
 */

package br.com.brjdevs.miyuki.commands;

import br.com.brjdevs.miyuki.modules.cmds.manager.CommandManager;
import br.com.brjdevs.miyuki.modules.cmds.manager.PermissionsModule;
import br.com.brjdevs.miyuki.modules.db.I18nModule;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


public class Commands {
	public static CommandBuilder buildSimple() {
		return new CommandBuilder();
	}

	public static CommandBuilder buildSimple(Function<String, String> usageProvider) {
		return new CommandBuilder(usageProvider);
	}

	public static CommandBuilder buildSimple(String translatableUsage) {
		return new CommandBuilder(translatableUsage);
	}

	public static CommandBuilder buildSimple(long permRequired) {
		return new CommandBuilder(permRequired);
	}

	public static CommandBuilder buildSimple(Function<String, String> usageProvider, long permRequired) {
		return new CommandBuilder(usageProvider, permRequired);
	}

	public static CommandBuilder buildSimple(String translatableUsage, long permRequired) {
		return new CommandBuilder(translatableUsage, permRequired);
	}


	public static TreeCommandBuilder buildTree() {
		return new TreeCommandBuilder();
	}

	public static TreeCommandBuilder buildTree(long permRequired) {
		return new TreeCommandBuilder(permRequired);
	}

	public static AliasCommandBuilder buildAlias() {
		return new AliasCommandBuilder();
	}

	public static class AliasCommandBuilder {
		public ICommand of(final ICommand cmd, final String name) {
			return new ICommand() {
				@Override
				public void run(CommandEvent event) {
					cmd.run(event.createChild(cmd, event.getArgs()));
				}

				@Override
				public long retrievePerm() {
					return cmd.retrievePerm();
				}

				@Override
				public boolean sendStartTyping() {
					return cmd.sendStartTyping();
				}

				@Override
				public String toString(String language) {
					return String.format(I18nModule.getLocalized("alias.of", language), name);
				}
			};
		}

		public ICommand of(final ICommand cmd, final String args, final String name) {
			return new ICommand() {
				@Override
				public void run(CommandEvent event) {
					cmd.run(event.createChild(cmd, args));
				}

				@Override
				public long retrievePerm() {
					return cmd.retrievePerm();
				}

				@Override
				public boolean sendStartTyping() {
					return cmd.sendStartTyping();
				}

				@Override
				public String toString(String language) {
					return String.format(I18nModule.getLocalized("alias.of", language), name + " " + args);
				}
			};
		}
	}

	public static class CommandBuilder {
		private static final Function<String, String> DEFAULT_NOOP_PROVIDER = (s) -> null;
		private Consumer<CommandEvent> action = null;
		private long permRequired = PermissionsModule.RUN_CMDS;
		private Function<String, String> usageProvider = DEFAULT_NOOP_PROVIDER;

		public CommandBuilder() {
		}

		public CommandBuilder(Function<String, String> usageProvider) {
			setDynamicUsage(usageProvider);
		}

		public CommandBuilder(String translatableUsage) {
			setDynamicUsage((lang) -> I18nModule.getLocalized(translatableUsage, lang));
		}

		public CommandBuilder(long permRequired) {
			this.permRequired = permRequired;
		}

		public CommandBuilder(Function<String, String> usageProvider, long permRequired) {
			setDynamicUsage(usageProvider);
			setPermRequired(permRequired);
		}

		public CommandBuilder(String translatableUsage, long permRequired) {
			setTranslatableUsage(translatableUsage);
			setPermRequired(permRequired);
		}

		public CommandBuilder setAction(Consumer<CommandEvent> consumer) {
			action = consumer;
			return this;
		}

		private CommandBuilder setPermRequired(long value) {
			permRequired = value;
			return this;
		}

		private CommandBuilder setTranslatableUsage(String translatableUsage) {
			return setDynamicUsage((lang) -> I18nModule.getLocalized(translatableUsage, lang));
		}

		private CommandBuilder setDynamicUsage(Function<String, String> provider) {
			usageProvider = provider;
			return this;
		}

		public ICommand build() {
			if (usageProvider == DEFAULT_NOOP_PROVIDER) {
				LogManager.getLogger("CommandBuilder - Unsafe").warn("No Usage was provided to the Command being built! Please set a Usage to it!", new Throwable("Stacktrace:"));
			}

			return new ICommand() {
				@Override
				public void run(CommandEvent event) {
					action.accept(event);
				}

				@Override
				public long retrievePerm() {
					return permRequired;
				}

				@Override
				public String toString(String language) {
					return usageProvider.apply(language);
				}
			};
		}
	}

	public static class TreeCommandBuilder {
		private final Map<String, ICommand> SUBCMDS = new HashMap<>();
		private final Function<String, String> USAGE_IMPL = (lang) -> {
			Holder<StringBuilder> b = new Holder<>();
			Holder<Boolean> first = new Holder<>();

			b.var = new StringBuilder(I18nModule.getLocalized("tree.subcmds", lang) + ":");
			first.var = true;
			SUBCMDS.forEach((cmdName, cmd) -> {
				String usage = (cmd == null) ? null : cmd.toString(lang);
				if (usage == null || usage.isEmpty()) return;
				if (first.var) {
					first.var = false;
				}
				String a = "\n - " + (cmdName.isEmpty() ? "(" + I18nModule.getLocalized("tree.default", lang) + ")" : cmdName) + ": " + usage.replace("\n", "\n    ");
				b.var.append(a);
			});
			if (first.var) return null;
			return b.var.toString();
		};
		private final BiConsumer<CommandEvent, Map<String, ICommand>> NOT_FOUND_IMPL = (event, map) -> event.getAnswers().invalidargs().queue();
		private final BiConsumer<CommandEvent, Map<String, ICommand>> NOT_FOUND_REDIRECT = (event, map) -> CommandManager.execute(event.createChild(map.get(""), event.getArgs()));
		private long permRequired = PermissionsModule.RUN_CMDS;
		private Function<String, String> usageProvider = USAGE_IMPL;
		private BiConsumer<CommandEvent, Map<String, ICommand>> onNotFound = NOT_FOUND_IMPL;
		public TreeCommandBuilder() {
			addDefault((ICommand) null);
		}

		public TreeCommandBuilder(long permRequired) {
			this();
			setPermRequired(permRequired);
		}

		public TreeCommandBuilder addCommand(String cmd, ICommand command) {
			SUBCMDS.put(cmd.toLowerCase(), command);
			return this;
		}

		public TreeCommandBuilder addDefault(ICommand command) {
			return addCommand("", command);
		}

		public TreeCommandBuilder addCommand(String cmd, String alias) {
			ICommand base = SUBCMDS.get(alias);
			return addCommand(cmd, buildAlias().of(base, alias));
		}

		public TreeCommandBuilder onNotFound(NotFoundAction action) {
			switch (action) {
				case SHOW_OPTIONS:
					onNotFound = NOT_FOUND_IMPL;
				case REDIRECT:
					onNotFound = NOT_FOUND_REDIRECT;
			}

			return this;
		}

		public TreeCommandBuilder onNotFound(BiConsumer<CommandEvent, Map<String, ICommand>> action) {
			onNotFound = action;
			return this;
		}

		public TreeCommandBuilder addDefault(String alias) {
			return addCommand("", alias);
		}

		private TreeCommandBuilder setPermRequired(long value) {
			permRequired = value;
			return this;
		}

		public ICommand build() {
			return Commands.buildSimple(USAGE_IMPL, permRequired).setAction(event -> {
				String[] args = event.getArgs(2);
				ICommand cmd = SUBCMDS.get(args[0].toLowerCase());
				if (cmd == null) onNotFound.accept(event, SUBCMDS);
				else CommandManager.execute(event.createChild(cmd, args[1]));
			}).build();
		}

		public enum NotFoundAction {
			SHOW_OPTIONS, REDIRECT
		}
	}
}
