package br.com.brjdevs.miyuki.modules.cmds;


import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.Commands.TreeCommandBuilder.NotFoundAction;
import br.com.brjdevs.miyuki.commands.Holder;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Command;
import br.com.brjdevs.miyuki.modules.db.UserModule;
import br.com.brjdevs.miyuki.utils.DiscordUtils;
import net.dv8tion.jda.core.MessageBuilder;

import static br.com.brjdevs.miyuki.modules.db.I18nModule.getLocale;
import static br.com.brjdevs.miyuki.modules.db.I18nModule.getLocalized;

@Module(name = "cmds.user")
public class UserCmd {
	@Command("user")
	private static ICommand createCommand() {
		return Commands.buildTree().onNotFound(NotFoundAction.REDIRECT)
			.addCommand("info", Commands.buildSimple("user.info.usage")
				.setAction(event -> {
					String[] users = event.getArgs(0);
					if (users.length == 0) {
						users = new String[]{event.getAuthor().getId()};
					}

					Holder<Boolean> any = new Holder<>(false);

					for (String userId : users) {
						net.dv8tion.jda.core.entities.User user = event.getJDA().getUserById(DiscordUtils.processId(userId));
						if (user == null) continue;
						any.var = true;

						event.awaitTyping(false).sendMessage(new MessageBuilder().setEmbed(UserModule.createEmbed(UserModule.fromDiscord(user), event.getJDA(), getLocale(event), event.getGuild().getGuild(event.getJDA()))).build()).queue();
					}

					if (!any.var) {
						net.dv8tion.jda.core.entities.User user = event.getAuthor();
						any.var = true;
						event.awaitTyping(false).sendMessage(new MessageBuilder().setEmbed(UserModule.createEmbed(UserModule.fromDiscord(user), event.getJDA(), getLocale(event), event.getGuild().getGuild(event.getJDA()))).build()).queue();
					}

				}).build()
			)
			.addCommand("lang",
				Commands.buildSimple("user.lang.usage")
					.setAction(event -> {
						UserModule.fromDiscord(event.getAuthor()).setLang(event.getArgs().trim());
						if (event.getArgs().trim().isEmpty())
							event.getAnswers().announce(getLocalized("user.lang.setNone", event)).queue();
						else
							event.getAnswers().announce(String.format(getLocalized("user.lang.set", event), event.getArgs().trim())).queue();
					})
					.build()
			)
			.addDefault("info")
			.build();
	}
}
